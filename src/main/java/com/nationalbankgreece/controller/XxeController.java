package com.nationalbankgreece.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/xml")
public class XxeController {

    // XXE — external entity processing not disabled
    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_XML_VALUE)
    public String processXmlTransfer(@RequestBody String xmlPayload) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // XXE: external entities enabled (default), DTD processing not disabled
            // Attack: <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new ByteArrayInputStream(xmlPayload.getBytes(StandardCharsets.UTF_8))
            );

            NodeList amounts = doc.getElementsByTagName("amount");
            NodeList froms   = doc.getElementsByTagName("from");
            NodeList tos     = doc.getElementsByTagName("to");

            String result = String.format(
                "Transfer request: from=%s to=%s amount=%s",
                froms.getLength() > 0 ? froms.item(0).getTextContent() : "?",
                tos.getLength()   > 0 ? tos.item(0).getTextContent()   : "?",
                amounts.getLength() > 0 ? amounts.item(0).getTextContent() : "?"
            );

            return result;

        } catch (Exception e) {
            // Full exception in response — may include file contents if XXE succeeds
            return "Error: " + e.getMessage();
        }
    }

    // SSRF — URL parameter fetched server-side without whitelist
    @GetMapping("/fetch")
    public String fetchUrl(@RequestParam String url) {
        try {
            java.net.URL target = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) target.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            byte[] data = conn.getInputStream().readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Fetch error: " + e.getMessage();
        }
    }
}
