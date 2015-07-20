/**
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
package org.commonjava.maven.ext.manip.state;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.ext.manip.impl.DependencyManipulator;
import org.commonjava.maven.ext.manip.util.IdUtils;

/**
 * Captures configuration relating to dependency alignment from the POMs. Used by {@link DependencyManipulator}.
 */
public class DependencyState
    implements State
{
    /**
     * The String that needs to be prepended a system property to make it a dependencyExclusion.
     * For example to exclude junit alignment for the GAV (org.groupId:artifactId)<br/>
     * <code>-DdependencyExclusion.junit:junit@org.groupId:artifactId</code>
     */
    public static final String DEPENDENCY_EXCLUSION_PREFIX = "dependencyExclusion.";

    /**
     * Enables strict checking of non-exclusion dependency versions before aligning to the given BOM dependencies.
     * For example, <code>1.1</code> will match <code>1.1-rebuild-1</code> in strict mode, but <code>1.2</code> will not.
     */
    public static final String STRICT_DEPENDENCIES = "strictAlignment";

    /**
     * When false, strict version-alignment violations will be reported in the warning log-level, but WILL NOT FAIL THE BUILD. When true, the build
     * will fail if such a violation is detected. Default value is false.
     */
    public static final String STRICT_VIOLATION_FAILS = "strictViolationFails";

    /**
     * Two possible formats currently supported for version property output:</br>
     * <code>version.group</code></br>
     * <code>version.group.artifact</code></br>
     * <code>none</code> (equates to off)</br>
     * Configured by the property <code>-DversionPropertyFormat=[VG|VGA|NONE]</code>
     */
    public static enum VersionPropertyFormat
    {
        VG,
        VGA,
        NONE;
    }

    /**
     * The name of the property which contains the GAV of the remote pom from which to retrieve dependency management
     * information. <br />
     *<code>-DdependencyManagement:org.foo:bar-dep-mgmt:1.0</code>
     */
    public static final String DEPENDENCY_MANAGEMENT_POM_PROPERTY = "dependencyManagement";

    private final List<ProjectVersionRef> depMgmt;

    private final boolean overrideTransitive;

    private final boolean overrideDependencies;

    private final boolean strict;

    private final boolean failOnStrictViolation;

    /**
     * Used to store mappings of old property -> new version.
     */
    private final HashMap<String, String> versionPropertyUpdateMap = new HashMap<String, String>();


    public DependencyState( final Properties userProps )
    {
        overrideTransitive = Boolean.valueOf( userProps.getProperty( "overrideTransitive", "true" ) );
        overrideDependencies = Boolean.valueOf( userProps.getProperty( "overrideDependencies", "true" ) );
        strict = Boolean.valueOf( userProps.getProperty( STRICT_DEPENDENCIES, "false" ) );
        failOnStrictViolation = Boolean.valueOf( userProps.getProperty( STRICT_VIOLATION_FAILS, "false" ) );

        depMgmt = IdUtils.parseGAVs( userProps.getProperty( DEPENDENCY_MANAGEMENT_POM_PROPERTY ) );
    }

    /**
     * Enabled ONLY if propertyManagement is provided in the user properties / CLI -D options.
     *
     * @see org.commonjava.maven.ext.manip.state.State#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return depMgmt != null && !depMgmt.isEmpty();
    }

    /**
     * Whether to override unmanaged transitive dependencies in the build. Has the effect of adding (or not) new entries
     * to dependency management when no matching dependency is found in the pom. Defaults to true.
     */
    public boolean getOverrideTransitive()
    {
        return overrideTransitive;
    }

    /**
     * Whether to override managed dependencies in the build. Defaults to true.
     */
    public boolean getOverrideDependencies()
    {
        return overrideDependencies;
    }

    public List<ProjectVersionRef> getRemoteDepMgmt()
    {
        return depMgmt;
    }

    public boolean getStrict()
    {
        return strict;
    }

    public HashMap<String, String> getVersionPropertyUpdateMap()
    {
        return versionPropertyUpdateMap;
    }

    public boolean getFailOnStrictViolation()
    {
        return failOnStrictViolation;
    }

}