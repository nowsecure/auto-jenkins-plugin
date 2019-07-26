package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedMap;

import org.acegisecurity.AccessDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.converters.SingleValueConverterWrapper;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import com.thoughtworks.xstream.mapper.DefaultMapper;
import com.thoughtworks.xstream.mapper.Mapper;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testNormalize() throws Exception {
        ItemGroup group = new ItemGroup() {

            @Override
            public File getRootDir() {
                return new File(".");
            }

            @Override
            public void save() throws IOException {
            }

            @Override
            public String getDisplayName() {
                return "";
            }

            @Override
            public String getFullName() {
                return "";
            }

            @Override
            public String getFullDisplayName() {
                return "";
            }

            @Override
            public Collection getItems() {
                return Arrays.asList();
            }

            @Override
            public String getUrl() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getUrlChildPrefix() {
                return "";
            }

            @Override
            public Item getItem(String name) throws AccessDeniedException {
                return null;
            }

            @Override
            public File getRootDirFor(Item child) {
                return new File(".");
            }

            @Override
            public void onRenamed(Item item, String oldName, String newName) throws IOException {

            }

            @Override
            public void onDeleted(Item item) throws IOException {
            }
        };
        Job job = new Job(group, "name") {

            @Override
            public boolean isBuildable() {
                return false;
            }

            @Override
            protected SortedMap _getRuns() {
                return null;
            }

            @Override
            protected void removeRun(Run run) {
            }
        };
        Run run = new Run(job) {
        };
        Assert.assertNull(NSAutoPlugin.normalize(run, null));
        Assert.assertEquals("test", NSAutoPlugin.normalize(run, "test"));
        Assert.assertNotEquals("${HOME}", NSAutoPlugin.normalize(run, "${HOME}"));
        Assert.assertEquals("${test}", NSAutoPlugin.normalize(run, "${test}"));
    }

    @Test
    public void testSerialize() throws Exception {
        HierarchicalStreamWriter writer = new JsonWriter(new StringWriter());
        NSAutoPlugin plugin = new NSAutoPlugin("api", "group", "binary", "desc", true, 30, true, 75, "key", true);
        Jenkins.XSTREAM2.marshal(plugin, writer);
        plugin.getProxySettings().setProxyServer("host");
        plugin.getProxySettings().setProxyPort(8080);
        plugin.getProxySettings().setUserName("user");
        plugin.getProxySettings().setProxyPass("password");
        plugin.getProxySettings().setNoProxyHost("localhost");
        Jenkins.XSTREAM2.marshal(plugin, writer);

        XStream2 xs = new XStream2();
        XStreamDOM dom = XStreamDOM.from(xs, plugin);
        dom.unmarshal(xs);

        // RobustReflectionConverter converter = new
        // RobustReflectionConverter(mapper, new PureJavaReflectionProvider());
        // MarshallingContext context = new TreeMarshaller(writer,
        // converterLookup, mapper);
        // converter.marshal(plugin, writer, context);

        // ObjectOutputStream out = new ObjectOutputStream(new
        // ByteArrayOutputStream());
        // out.writeObject(plugin);
        // Assert.assertTrue(converter.canConvert(plugin.getClass()));
        // converter.marshal(plugin, writer, context);
    }
}
