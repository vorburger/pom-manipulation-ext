/*
 * Copyright (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.ext.manip.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.commonjava.maven.ext.manip.ManipulationException;
import org.commonjava.maven.ext.manip.io.JSONIO;
import org.commonjava.maven.ext.manip.io.JSONIOTest;
import org.commonjava.maven.ext.manip.model.Project;
import org.commonjava.maven.ext.manip.state.JSONState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JSONManipulatorTest
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private JSONManipulator jsonManipulator = new JSONManipulator();
    private File npmFile;
    private File pluginFile;

    @Rule
    public TemporaryFolder tf = new TemporaryFolder(  );

    @Before
    public void setup() throws IOException, IllegalAccessException, URISyntaxException
    {
        FieldUtils.writeField(jsonManipulator, "jsonIO", new JSONIO(), true);

        URL resource = JSONIOTest.class.getResource( "npm-shrinkwrap.json");
        npmFile = tf.newFile();
        pluginFile = tf.newFile();

        FileUtils.copyURLToFile( resource, npmFile );

        URL resource2 = JSONIOTest.class.getResource( "amg-plugin-registry.json");
        FileUtils.copyURLToFile( resource2, pluginFile );
    }

    @Test(expected = ManipulationException.class)
    public void testNotFound()
        throws Exception
    {
        String modifyPath = "$.I.really.do.not.exist.repository.url";

        File target = tf.newFile();
        FileUtils.copyFile( npmFile, target );
        Project project = new Project( null, target, null );

        jsonManipulator.internalApplyChanges( project, new JSONState.JSONOperation( target.getName(), modifyPath, null) );
    }

    @Test
    public void updateURL () throws ManipulationException, IOException
    {
        String modifyPath = "$.repository.url";

        File target = tf.newFile();
        FileUtils.copyFile( pluginFile, target );
        Project project = new Project( null, target, null );

        jsonManipulator.internalApplyChanges( project, new JSONState.JSONOperation( target.getName(), modifyPath,
                                                                                            "https://maven.repository.redhat.com/ga/" ) );
        assertTrue( FileUtils.readFileToString( target ).contains( "https://maven.repository.redhat.com/ga/" ) );
        assertFalse( FileUtils.contentEquals( pluginFile, target ) );
    }

    @Test(expected = ManipulationException.class)
    public void testStateConstructionNoEscape() throws ManipulationException
    {
        Properties p = new Properties();
        p.put ("jsonUpdate",
               "amg-plugin-registry.json:$..plugins[0].description:CORS,and:controlling");

        new JSONState( p );
    }
    @Test
    public void testStateConstructionEscaping() throws ManipulationException
    {
        Properties p = new Properties();
        p.put ("jsonUpdate",
               "amg-plugin-registry.json:$xpath-with\\:and\\,:replace with space and \\,\\:controlling\\:access_to_resources_outside_of_an_originating_domain\\,and_to_this_domain.");


        JSONState js = new JSONState( p );
    }
}
