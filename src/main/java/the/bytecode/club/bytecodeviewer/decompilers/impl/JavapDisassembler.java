package the.bytecode.club.bytecodeviewer.decompilers.impl;

import me.konloch.kontainer.io.DiskReader;
import me.konloch.kontainer.io.DiskWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import the.bytecode.club.bytecodeviewer.Configuration;
import the.bytecode.club.bytecodeviewer.Constants;
import the.bytecode.club.bytecodeviewer.decompilers.InternalDecompiler;
import the.bytecode.club.bytecodeviewer.gui.components.SystemConsole;
import the.bytecode.club.bytecodeviewer.resources.ExternalResources;
import the.bytecode.club.bytecodeviewer.util.MiscUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static the.bytecode.club.bytecodeviewer.Constants.fs;
import static the.bytecode.club.bytecodeviewer.api.ExceptionUI.KONLOCH;
import static the.bytecode.club.bytecodeviewer.api.ExceptionUI.SEND_STACKTRACE_TO;

/***************************************************************************
 * Bytecode Viewer (BCV) - Java & Android Reverse Engineering Suite        *
 * Copyright (C) 2014 Kalen 'Konloch' Kinloch - http://bytecodeviewer.com  *
 *                                                                         *
 * This program is free software: you can redistribute it and/or modify    *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 ***************************************************************************/

/**
 * Javap disassembler
 *
 * https://github.com/Konloch/bytecode-viewer/issues/93
 *
 * @author Konloch
 * @since 07/11/2021
 */

public class JavapDisassembler extends InternalDecompiler
{
    @Override
    public String decompileClassNode(ClassNode cn, byte[] b)
    {
        if(!ExternalResources.getSingleton().hasJavaToolsSet())
            return "Set Java Tools Path!";
        
        final File tempDirectory = new File(Constants.tempDirectory + fs + MiscUtils.randomString(32) + fs);
        tempDirectory.mkdir();
        final File tempClass = new File(Constants.tempDirectory + fs + "temp" + MiscUtils.randomString(32) + ".class");
        
        DiskWriter.replaceFileBytes(tempClass.getAbsolutePath(), b, false);
    
        SystemConsole sysOut = null;
        try
        {
            URLClassLoader child = new URLClassLoader(
                    new URL[] {new File(Configuration.javaTools).toURI().toURL()},
                    this.getClass().getClassLoader()
            );
            //Class<?> javap = child.loadClass("com.sun.tools.javap.Main");
            Class<?> javap = Class.forName("com.sun.tools.javap.Main", true, child);
            Method main = javap.getMethod("main", String[].class);
            Object cl = javap.newInstance();
            
            //pipe sys out
            sysOut = new SystemConsole("");
            
            //invoke Javap
            main.invoke(cl, (Object) new String[]{"-c", "-l", "-constants", tempClass.getAbsolutePath()});
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            tempClass.delete();
        }
        
        if(sysOut != null)
        {
            sysOut.finished();
            return sysOut.getTextAreaOutputStreamOut().getBuffer().toString();
        }
        
        return SEND_STACKTRACE_TO;
    }

    @Override
    public void decompileToZip(String sourceJar, String zipName) {

    }
}
