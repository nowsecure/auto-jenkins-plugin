package com.nowsecure.auto.jenkins.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.BiPredicate;

public class IOHelper {
    private static final String GET = "GET";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String POST = "POST";
    private static final int TIMEOUT = 60000;

    public static byte[] load(String file) throws IOException {
        return Files.readAllBytes(Paths.get(file));
    }

    public static File find(File parent, String name) throws IOException {
        if (name == null || name.length() == 0) {
            return null;
        }
        final File file = new File(name);
        if (file.isFile() && file.exists()) {
            return file;
        }
        Optional<Path> matched = Files
                .find(Paths.get(parent.getCanonicalPath()), 10, new BiPredicate<Path, BasicFileAttributes>() {
                    @Override
                    public boolean test(Path t, BasicFileAttributes u) {
                        return t.toString().endsWith(file.getName());
                    }
                }, FileVisitOption.FOLLOW_LINKS).distinct().findFirst();
        if (matched.isPresent()) {
            return matched.get().toFile();
        }
        return null;
    }

    public static void save(String path, String contents) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(contents.trim());
        }
    }

    public static byte[] load(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static String get(String uri, String apiKey) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(GET);
        con.setRequestProperty(CONTENT_TYPE, "application/json");
        con.setRequestProperty(AUTHORIZATION, "Bearer " + apiKey);
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        con.setInstanceFollowRedirects(false);
        InputStream in = con.getInputStream();
        String json = new String(load(in), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return json.trim();
    }

    public static String upload(String uri, String apiKey, String file) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(POST);
        con.setRequestProperty(CONTENT_TYPE, "application/json");
        con.setRequestProperty(AUTHORIZATION, "Bearer " + apiKey);
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        con.setInstanceFollowRedirects(false);
        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        byte[] binary = load(file);
        out.write(binary);
        out.flush();
        out.close();
        InputStream in = con.getInputStream();
        String json = new String(load(in), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return json;
    }

}
