/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.connector.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.teiid.connector.api.ConnectorException;
import org.teiid.connector.api.ExecutionContext;
import org.teiid.connector.jdbc.translator.TranslatedCommand;
import org.teiid.connector.jdbc.translator.Translator;
import org.teiid.connector.language.Command;

import com.metamatrix.cdk.api.TranslationUtility;
import com.metamatrix.cdk.unittest.FakeTranslationFactory;
import com.metamatrix.core.MetaMatrixRuntimeException;
import com.metamatrix.query.function.metadata.FunctionMetadataReader;
import com.metamatrix.query.function.metadata.FunctionMethod;

public class TranslationHelper {
	
    public static final String PARTS_VDB = "/PartsSupplier.vdb"; //$NON-NLS-1$
    public static final String BQT_VDB = "/bqt.vdb"; //$NON-NLS-1$

    public static Command helpTranslate(String vdbFileName, String sql) {
    	return helpTranslate(vdbFileName, null, sql);
    }
    
    public static Command helpTranslate(String vdbFileName, String udf, String sql) {
    	TranslationUtility util = null;
    	if (PARTS_VDB.equals(vdbFileName)) {
    		util = new TranslationUtility(TranslationHelper.class.getResource(vdbFileName));
    	} else if (BQT_VDB.equals(vdbFileName)){
    		util = FakeTranslationFactory.getInstance().getBQTTranslationUtility();
    	} else {
    		Assert.fail("unknown vdb"); //$NON-NLS-1$
    	}
    	
    	if (udf != null) {
    		try {
				Collection <FunctionMethod> methods = FunctionMetadataReader.loadFunctionMethods(TranslationHelper.class.getResource(udf).openStream());
				util.setUDF(methods);
			} catch (IOException e) {
				throw new MetaMatrixRuntimeException("failed to load UDF");
			}
    	}
        return util.parseCommand(sql);        
    }    

	public static void helpTestVisitor(String vdb, String input, String expectedOutput, Translator translator) throws ConnectorException {
		helpTestVisitor(vdb,null,input, expectedOutput, translator);
	}
	
	public static void helpTestVisitor(String vdb, String udf, String input, String expectedOutput, Translator translator) throws ConnectorException {
	    // Convert from sql to objects
	    Command obj = helpTranslate(vdb, udf, input);
	    
	    helpTestVisitor(expectedOutput, translator, obj);
	}	

	public static void helpTestVisitor(String expectedOutput,
			Translator translator, Command obj) throws ConnectorException {
		TranslatedCommand tc = new TranslatedCommand(Mockito.mock(ExecutionContext.class), translator);
	    tc.translateCommand(obj);
	    assertEquals("Did not get correct sql", expectedOutput, tc.getSql());             //$NON-NLS-1$
	}

}
