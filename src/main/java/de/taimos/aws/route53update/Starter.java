package de.taimos.aws.route53update;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.amazonaws.util.EC2MetadataUtils;
import de.taimos.httputils.WS;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Starter {

    private static String region;


    /**
     * @param args
     *         - the args
     */
    public static void main(String[] args) {
        OptionParser p = new OptionParser();
        OptionSpec<String> domainOpt = p.accepts("domain", "the hosted zone name").withRequiredArg().ofType(String.class).required();
        OptionSpec<String> hostOpt = p.accepts("host", "the host name (default instanceID)").withRequiredArg().ofType(String.class);
        OptionSpec<String> regionOpt = p.accepts("region", "the AWS region (default eu-west-1)").withRequiredArg().ofType(String.class);
        OptionSpec<String> modeOpt = p.accepts("mode", "the IP resolve mode (private|public|external)").withRequiredArg().ofType(
                String.class);
        p.accepts("private", "use private IP (deprecated; use '--mode private')");

        final OptionSet options;
        try {
            options = p.parse(args);
        } catch (OptionException e1) {
            try {
                p.printHelpOn(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(1);
            // compiler doesn't understand exit(1)
            throw new RuntimeException();
        }

        if (options.has(regionOpt)) {
            Starter.region = options.valueOf(regionOpt);
        } else {
            Starter.region = "eu-west-1";
        }

        String domain = Starter.getDomainFromArguments(domainOpt, options);
        String host = Starter.getHostFromArguments(hostOpt, options, domain);
        String zoneId = Starter.findZoneId(domain);

        ResourceRecordSet set = Starter.findCurrentSet(zoneId, host);
        List<Change> changeSet = new ArrayList<>();
        if (set != null) {
            System.out.println("Deleting current set: " + set);
            changeSet.add(new Change("DELETE", set));
        }

        final String mode;
        if (!options.has(modeOpt)) {
            mode = options.has("private") ? "private" : "public";
        } else {
            mode = options.valueOf(modeOpt);
        }

        final ResourceRecordSet rrs = new ResourceRecordSet(host, RRType.A).withTTL(60L).withResourceRecords(getResourceRecord(mode));
        System.out.println("Creating new set: " + rrs);
        changeSet.add(new Change("CREATE", rrs));

        try {
            AmazonRoute53Client cl = Starter.createClient();
            ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch(changeSet));
            cl.changeResourceRecordSets(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String getHostFromArguments(OptionSpec<String> hostOpt, OptionSet options, String domain) {
        String hostpart;
        if (options.has(hostOpt)) {
            hostpart = options.valueOf(hostOpt);
        } else {
            hostpart = EC2MetadataUtils.getInstanceId();
        }
        String host = hostpart + "." + domain;
        System.out.println("Host: " + host);
        return host;
    }


    private static String getDomainFromArguments(OptionSpec<String> domainOpt, OptionSet options) {
        String domain = options.valueOf(domainOpt);
        // Add dot for DNS
        if (!domain.endsWith(".")) {
            domain = domain + ".";
        }
        System.out.println("Domain: " + domain);
        return domain;
    }


    private static ResourceRecord getResourceRecord(String mode) {
        final String ipAddress;
        switch (mode) {
            case "private":
                ipAddress = EC2MetadataUtils.getPrivateIpAddress();
                break;
            case "public":
                ipAddress = Starter.getMetadata("public-ipv4");
                break;
            case "external":
                ipAddress = Starter.getExternalIP();
                break;
            default:
                throw new RuntimeException("Invalid IP resolve mode " + mode);
        }
        return new ResourceRecord(ipAddress);
    }


    private static String getMetadata(String value) {
        HttpResponse res = WS.url("http://169.254.169.254/latest/meta-data/{type}").pathParam("type", value).get();
        return WS.getResponseAsString(res);
    }


    private static String getExternalIP() {
        HttpResponse res = WS.url("http://icanhazip.com").get();
        return WS.getResponseAsString(res);
    }


    private static ResourceRecordSet findCurrentSet(String zoneId, String host) {
        AmazonRoute53Client cl = Starter.createClient();
        ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest(zoneId);
        ListResourceRecordSetsResult sets = cl.listResourceRecordSets(req);
        List<ResourceRecordSet> recordSets = sets.getResourceRecordSets();
        for (ResourceRecordSet rrs : recordSets) {
            if (rrs.getName().equals(host)) {
                return rrs;
            }
        }
        return null;
    }


    private static String findZoneId(String domain) {
        AmazonRoute53Client cl = Starter.createClient();
        ListHostedZonesResult zones = cl.listHostedZones();
        List<HostedZone> hostedZones = zones.getHostedZones();
        for (HostedZone hz : hostedZones) {
            if (hz.getName().equals(domain)) {
                return hz.getId();
            }
        }
        throw new RuntimeException("Cannot find zone: " + domain);
    }


    private static AmazonRoute53Client createClient() {
        AmazonRoute53Client cl = new AmazonRoute53Client();
        cl.setRegion(Region.getRegion(Regions.fromName(Starter.region)));
        return cl;
    }
}
