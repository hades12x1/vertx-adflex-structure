package vn.eway.service.io.elasticsearch;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Created by chipn@eway.vn on 2/7/17.
 */
public class ConnectionString {

    private static final String PREFIX = "es://";
    private static final String UTF_8 = "UTF-8";
    private final List<String> hosts;
    private String connectionString;
    private String index;
    private String type;
    private String userName;
    private String password;
    private Map<String, String> options;

    public ConnectionString(String connectionString) {
        this.connectionString = connectionString;

        if (!connectionString.startsWith(PREFIX)) {
            throw new IllegalArgumentException(format("The connection string is invalid. "
                    + "Connection strings must start with '%s'", PREFIX));
        }

        String unprocessedConnectionString = connectionString.substring(PREFIX.length());

        // Split out the user and host information
        String userAndHostInformation = null;
        int idx = unprocessedConnectionString.lastIndexOf("/");
        if (idx == -1) {
            if (unprocessedConnectionString.contains("?")) {
                throw new IllegalArgumentException("The connection string contains options without trailing slash");
            }
            userAndHostInformation = unprocessedConnectionString;
            unprocessedConnectionString = "";
        } else {
            userAndHostInformation = unprocessedConnectionString.substring(0, idx);
            unprocessedConnectionString = unprocessedConnectionString.substring(idx + 1);
        }

        // Split the user and host information
        String userInfo = null;
        String hostIdentifier = null;

        idx = userAndHostInformation.lastIndexOf("@");
        if (idx > 0) {
            userInfo = userAndHostInformation.substring(0, idx);
            hostIdentifier = userAndHostInformation.substring(idx + 1);
            int colonCount = countOccurrences(userInfo, ":");
            if (userInfo.contains("@") || colonCount > 1) {
                throw new IllegalArgumentException("The connection string contains invalid user information. "
                        + "If the username or password contains a colon (:) or an at-sign (@) then it must be urlencoded");
            }
            if (colonCount == 0) {
                userName = urldecode(userInfo);
            } else {
                idx = userInfo.indexOf(":");
                userName = urldecode(userInfo.substring(0, idx));
                password = urldecode(userInfo.substring(idx + 1), true);
            }
        } else {
            hostIdentifier = userAndHostInformation;
        }

        // Validate the hosts
        hosts = Collections.unmodifiableList(parseHosts(asList(hostIdentifier.split(","))));

        // Process the authDB section
        String nsPart = null;
        idx = unprocessedConnectionString.indexOf("?");
        if (idx == -1) {
            nsPart = unprocessedConnectionString;
            unprocessedConnectionString = "";
        } else {
            nsPart = unprocessedConnectionString.substring(0, idx);
            unprocessedConnectionString = unprocessedConnectionString.substring(idx + 1);
        }
        if (nsPart.length() > 0) {
            nsPart = urldecode(nsPart);
            idx = nsPart.indexOf(".");
            if (idx < 0) {
                index = nsPart;
                type = null;
            } else {
                index = nsPart.substring(0, idx);
                type = nsPart.substring(idx + 1);
            }
        } else {
            index = null;
            type = null;
        }

        options = parseOptions(unprocessedConnectionString);
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getConnectionString() {
        return connectionString;
    }

    private List<String> parseHosts(final List<String> rawHosts) {
        if (rawHosts.size() == 0) {
            throw new IllegalArgumentException("The connection string must contain at least one host");
        }
        List<String> hosts = new ArrayList<String>();
        for (String host : rawHosts) {
            if (host.length() == 0) {
                throw new IllegalArgumentException(format("The connection string contains an empty host '%s'. ", rawHosts));
            } else if (host.endsWith(".sock")) {
                throw new IllegalArgumentException(format("The connection string contains an invalid host '%s'. "
                        + "Unix Domain Socket which is not supported by the Java driver", host));
            } else if (host.startsWith("[")) {
                if (!host.contains("]")) {
                    throw new IllegalArgumentException(format("The connection string contains an invalid host '%s'. "
                            + "IPv6 address literals must be enclosed in '[' and ']' according to RFC 2732", host));
                }
                int idx = host.indexOf("]:");
                if (idx != -1) {
                    validatePort(host, host.substring(idx + 2));
                }
            } else {
                int colonCount = countOccurrences(host, ":");
                if (colonCount > 1) {
                    throw new IllegalArgumentException(format("The connection string contains an invalid host '%s'. "
                            + "Reserved characters such as ':' must be escaped according RFC 2396. "
                            + "Any IPv6 address literal must be enclosed in '[' and ']' according to RFC 2732.", host));
                } else if (colonCount == 1) {
                    validatePort(host, host.substring(host.indexOf(":") + 1));
                }
            }
            hosts.add(host);
        }
        return hosts;
    }

    private Map<String, String> parseOptions(final String optionsPart) {
        Map<String, String> optionsMap = new HashMap<String, String>();
        if (optionsPart.length() == 0) {
            return optionsMap;
        }

        for (final String part : optionsPart.split("&|;")) {
            if (part.length() == 0) {
                continue;
            }
            int idx = part.indexOf("=");
            if (idx >= 0) {
                String key = part.substring(0, idx).toLowerCase();
                String value = part.substring(idx + 1);

                optionsMap.put(key, urldecode(value));
            } else {
                throw new IllegalArgumentException(format("The connection string contains an invalid option '%s'. "
                        + "'%s' is missing the value delimiter eg '%s=value'", optionsPart, part, part));
            }
        }
        return optionsMap;
    }

    private void validatePort(final String host, final String port) {
        boolean invalidPort = false;
        try {
            int portInt = Integer.parseInt(port);
            if (portInt <= 0 || portInt > 65535) {
                invalidPort = true;
            }
        } catch (NumberFormatException e) {
            invalidPort = true;
        }
        if (invalidPort) {
            throw new IllegalArgumentException(format("The connection string contains an invalid host '%s'. "
                    + "The port '%s' is not a valid, it must be an integer between 0 and 65535", host, port));
        }
    }

    private int countOccurrences(final String haystack, final String needle) {
        return haystack.length() - haystack.replace(needle, "").length();
    }

    private String urldecode(final String input) {
        return urldecode(input, false);
    }

    private String urldecode(final String input, final boolean password) {
        try {
            return URLDecoder.decode(input, UTF_8);
        } catch (UnsupportedEncodingException e) {
            if (password) {
                throw new IllegalArgumentException("The connection string contained unsupported characters in the password.");
            } else {
                throw new IllegalArgumentException(format("The connection string contained unsupported characters: '%s'."
                        + "Decoding produced the following error: %s", input, e.getMessage()));
            }
        }
    }
}
