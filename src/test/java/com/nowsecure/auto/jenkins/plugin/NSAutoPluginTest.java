package com.nowsecure.auto.jenkins.plugin;

import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverterWrapper;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import com.thoughtworks.xstream.mapper.DefaultMapper;
import com.thoughtworks.xstream.mapper.Mapper;

import hudson.util.RobustReflectionConverter;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import jenkins.util.xstream.XStreamDOM;

public class NSAutoPluginTest {
    private HierarchicalStreamDriver driver;
    private DefaultConverterLookup converterLookup;
    private ReflectionProvider reflectionProvider;
    private Mapper mapper;

    @Before
    public void setup() throws Exception {
        final ClassAliasingMapper classAliasingMapper = new ClassAliasingMapper(
                new DefaultMapper(new ClassLoaderReference(getClass().getClassLoader())));
        classAliasingMapper.addClassAlias("x", getClass());
        // mapper = new DefaultImplementationsMapper(new
        // ArrayMapper(classAliasingMapper));
        mapper = new DefaultMapper(new ClassLoaderReference(getClass().getClassLoader()));

        reflectionProvider = new SunUnsafeReflectionProvider();
        driver = new XppDriver();

        converterLookup = new DefaultConverterLookup();
        converterLookup.registerConverter(new SingleValueConverterWrapper(new StringConverter()), 0);
        converterLookup.registerConverter(new SingleValueConverterWrapper(new IntConverter()), 0);
        converterLookup.registerConverter(new ArrayConverter(mapper), 0);
        converterLookup.registerConverter(new ReflectionConverter(mapper, reflectionProvider), -1);

    }

    @Test
    public void testSerialize() throws Exception {
        HierarchicalStreamWriter writer = new JsonWriter(new StringWriter());
        NSAutoPlugin plugin = new NSAutoPlugin("api", "group", "binary", "desc", true, 30, true, 75, "key", true);
        Jenkins.XSTREAM2.marshal(plugin, writer);
        plugin.getProxySettings().setProxyServer("host");
        plugin.getProxySettings().setProxyPort(8080);
        plugin.getProxySettings().setUserName("user");
        plugin.getProxySettings().setPassword("password");
        plugin.getProxySettings().setNoProxyHost("localhost");
        Jenkins.XSTREAM2.marshal(plugin, writer);
        
        XStream2 xs = new XStream2();
        XStreamDOM dom = XStreamDOM.from(xs, plugin);
        dom.unmarshal(xs);

        // RobustReflectionConverter converter = new
        // RobustReflectionConverter(mapper, new PureJavaReflectionProvider());
        // MarshallingContext context = new TreeMarshaller(writer,
        // converterLookup, mapper);
        //converter.marshal(plugin, writer, context);

        // ObjectOutputStream out = new ObjectOutputStream(new
        // ByteArrayOutputStream());
        // out.writeObject(plugin);
        // Assert.assertTrue(converter.canConvert(plugin.getClass()));
        // converter.marshal(plugin, writer, context);
    }
}
