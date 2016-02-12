package sk.eea.td.hp_client.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ApiTokenFactory {

    private Mac hmac;

    public ApiTokenFactory(String apiSecret) {
        try {
            if (isEmpty(apiSecret)) {
                throw new InvalidKeyException("Provided key cannot be empty or null!");
            }
            this.hmac = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes("UTF-8"), "HmacSHA256");
            hmac.init(secretKey);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new RuntimeException("Exception at initializing ApiTokenFactory", e);
        }
    }

    public String getApiToken(final Map<String, String> data) {
        Map<String, String> sortedMap = new TreeMap<>(data);
        final String body =
                String.join("&",
                        sortedMap.entrySet().stream().map(
                                e -> String.format("%s=%s", e.getKey(), e.getValue())
                        ).collect(Collectors.toList()));
        try {
            return Hex.encodeHexString(hmac.doFinal(body.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Exception when generating apiToken", e);
        }
    }
}
