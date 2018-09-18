package com.nowsecure.auto.jenkins.domain;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.jenkins.domain.AssessmentRequest;

public class AssessmentRequestTest {

    @Test
    public void testParse() throws URISyntaxException, IOException, ParseException {
        Path path = Paths.get(getClass().getClassLoader().getResource("upload.json").toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String json = new String(fileBytes);
        AssessmentRequest info = AssessmentRequest.fromJson(json);
        Assert.assertEquals("d2fc75a0-b2d8-48f5-a70d-eded118f3065", info.getAccount());
    }

}
