package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.notifyUrl}")
    private String vnp_NotifyUrl;

    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";

    public String createPaymentUrl(PaymentRequest request, HttpServletRequest servletRequest, String txnRef) throws UnsupportedEncodingException {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", VNP_VERSION);
        vnpParams.put("vnp_Command", VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Amount", String.valueOf((long)(request.getAmount() * 100))); // x100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", getClientIp(servletRequest));

        // create & expire date
        // VNPay yêu cầu timezone GMT+7 (giờ Việt Nam)
        // Sử dụng Asia/Ho_Chi_Minh thay vì Etc/GMT+7 (vì Etc/GMT+7 thực ra là GMT-7)
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(tz);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);
        vnpParams.put("vnp_CreateDate", formatter.format(calendar.getTime()));

        Calendar expire = Calendar.getInstance(tz);
        expire.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(expire.getTime()));

        // build hash data & query string
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            query.append('=');
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            query.append('&');
        }

        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("vnp_SecureHash=").append(vnp_SecureHash);

        return vnp_PayUrl + "?" + query.toString();
    }

    public boolean validateSignature(Map<String, String> params) {
        try {
            // extract vnp_SecureHash and remove it from map
            String secureHash = params.get("vnp_SecureHash");
            if (secureHash == null) return false;

            // build hash data string same order (TreeMap)
            Map<String, String> sorted = new TreeMap<>(params);
            sorted.remove("vnp_SecureHash");
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                 // ✅ QUAN TRỌNG: Phải URL encode lại giá trị như khi tạo URL
                hashData.append(entry.getKey())
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }
            String calculated = hmacSHA512(vnp_HashSecret, hashData.toString());
            return calculated.equalsIgnoreCase(secureHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // helper: extract params from request
    public Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                map.put(k, v[0]);
            }
        });
        return map;
    }
}
