package kr.co.wikibook.backend.health.controller;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

       @GetMapping("/health")
    public String health() {
        return "OK";
    }

 @GetMapping("/server-check")
public Map<String, String> serverCheck() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("server", InetAddress.getLocalHost().getHostName());
    map.put("ip", InetAddress.getLocalHost().getHostAddress());
    return map;
}

}
