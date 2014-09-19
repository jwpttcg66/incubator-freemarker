/*
 * Copyright 2014 Attila Szegedi, Daniel Dekany, Jonathan Revusky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package freemarker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import freemarker.core.DateBuiltins.iso_BI;
import freemarker.core.DateBuiltins.iso_utc_or_local_BI;
import freemarker.core.NodeBuiltins.ancestorsBI;
import freemarker.core.NodeBuiltins.childrenBI;
import freemarker.core.NodeBuiltins.node_nameBI;
import freemarker.core.NodeBuiltins.node_namespaceBI;
import freemarker.core.NodeBuiltins.node_typeBI;
import freemarker.core.NodeBuiltins.parentBI;
import freemarker.core.NodeBuiltins.rootBI;
import freemarker.core.NumericalBuiltins.absBI;
import freemarker.core.NumericalBuiltins.byteBI;
import freemarker.core.NumericalBuiltins.ceilingBI;
import freemarker.core.NumericalBuiltins.doubleBI;
import freemarker.core.NumericalBuiltins.floatBI;
import freemarker.core.NumericalBuiltins.floorBI;
import freemarker.core.NumericalBuiltins.intBI;
import freemarker.core.NumericalBuiltins.is_infiniteBI;
import freemarker.core.NumericalBuiltins.is_nanBI;
import freemarker.core.NumericalBuiltins.longBI;
import freemarker.core.NumericalBuiltins.number_to_dateBI;
import freemarker.core.NumericalBuiltins.roundBI;
import freemarker.core.NumericalBuiltins.shortBI;
import freemarker.core.SequenceBuiltins.chunkBI;
import freemarker.core.SequenceBuiltins.firstBI;
import freemarker.core.SequenceBuiltins.lastBI;
import freemarker.core.SequenceBuiltins.reverseBI;
import freemarker.core.SequenceBuiltins.seq_containsBI;
import freemarker.core.SequenceBuiltins.seq_index_ofBI;
import freemarker.core.SequenceBuiltins.sortBI;
import freemarker.core.SequenceBuiltins.sort_byBI;
import freemarker.core.StringBuiltins.booleanBI;
import freemarker.core.StringBuiltins.cap_firstBI;
import freemarker.core.StringBuiltins.capitalizeBI;
import freemarker.core.StringBuiltins.chop_linebreakBI;
import freemarker.core.StringBuiltins.evalBI;
import freemarker.core.StringBuiltins.j_stringBI;
import freemarker.core.StringBuiltins.js_stringBI;
import freemarker.core.StringBuiltins.json_stringBI;
import freemarker.core.StringBuiltins.lower_caseBI;
import freemarker.core.StringBuiltins.numberBI;
import freemarker.core.StringBuiltins.substringBI;
import freemarker.core.StringBuiltins.uncap_firstBI;
import freemarker.core.StringBuiltins.upper_caseBI;
import freemarker.core.StringBuiltins.word_listBI;
import freemarker.template.Configuration;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DateUtil;
import freemarker.template.utility.StringUtil;

/**
 * The {@code ?} operator used for things like {@code foo?upper_case}.
 */
abstract class BuiltIn extends Expression implements Cloneable {
    
    protected Expression target;
    protected String key;

    static final HashMap builtins = new HashMap();
    static {
        BuiltIn bi;
        
        builtins.put("abs", new absBI());
        builtins.put("ancestors", new ancestorsBI());
        builtins.put("boolean", new booleanBI());
        builtins.put("byte", new byteBI());
        builtins.put("c", new MiscellaneousBuiltins.cBI());
        builtins.put("cap_first", new cap_firstBI());
        builtins.put("capitalize", new capitalizeBI());
        builtins.put("ceiling", new ceilingBI());
        builtins.put("children", new childrenBI());
        builtins.put("chop_linebreak", new chop_linebreakBI());
        builtins.put("contains", new StringBuiltins.containsBI());        
        builtins.put("date", new MiscellaneousBuiltins.dateBI(TemplateDateModel.DATE));
        builtins.put("date_if_unknown", new DateBuiltins.dateType_if_unknownBI(TemplateDateModel.DATE));
        builtins.put("datetime", new MiscellaneousBuiltins.dateBI(TemplateDateModel.DATETIME));
        builtins.put("datetime_if_unknown", new DateBuiltins.dateType_if_unknownBI(TemplateDateModel.DATETIME));
        builtins.put("default", new ExistenceBuiltins.defaultBI());
        builtins.put("double", new doubleBI());
        builtins.put("ends_with", new StringBuiltins.ends_withBI());
        builtins.put("eval", new evalBI());
        builtins.put("exists", new ExistenceBuiltins.existsBI());
        builtins.put("first", new firstBI());
        builtins.put("float", new floatBI());
        builtins.put("floor", new floorBI());
        builtins.put("chunk", new chunkBI());
        builtins.put("has_content", new ExistenceBuiltins.has_contentBI());
        builtins.put("html", new StringBuiltins.htmlBI());
        builtins.put("if_exists", new ExistenceBuiltins.if_existsBI());
        builtins.put("index_of", new StringBuiltins.index_ofBI(false));
        builtins.put("int", new intBI());
        builtins.put("interpret", new Interpret());
        builtins.put("is_boolean", new MiscellaneousBuiltins.is_booleanBI());
        builtins.put("is_collection", new MiscellaneousBuiltins.is_collectionBI());
        bi = new MiscellaneousBuiltins.is_dateLikeBI();
        builtins.put("is_date", bi);  // misnomer
        builtins.put("is_date_like", bi);
        builtins.put("is_date_only", new MiscellaneousBuiltins.is_dateOfTypeBI(TemplateDateModel.DATE));
        builtins.put("is_unknown_date_like", new MiscellaneousBuiltins.is_dateOfTypeBI(TemplateDateModel.UNKNOWN));
        builtins.put("is_datetime", new MiscellaneousBuiltins.is_dateOfTypeBI(TemplateDateModel.DATETIME));
        builtins.put("is_directive", new MiscellaneousBuiltins.is_directiveBI());
        builtins.put("is_enumerable", new MiscellaneousBuiltins.is_enumerableBI());
        builtins.put("is_hash_ex", new MiscellaneousBuiltins.is_hash_exBI());
        builtins.put("is_hash", new MiscellaneousBuiltins.is_hashBI());
        builtins.put("is_infinite", new is_infiniteBI());
        builtins.put("is_indexable", new MiscellaneousBuiltins.is_indexableBI());
        builtins.put("is_macro", new MiscellaneousBuiltins.is_macroBI());
        builtins.put("is_method", new MiscellaneousBuiltins.is_methodBI());
        builtins.put("is_nan", new is_nanBI());
        builtins.put("is_node", new MiscellaneousBuiltins.is_nodeBI());
        builtins.put("is_number", new MiscellaneousBuiltins.is_numberBI());
        builtins.put("is_sequence", new MiscellaneousBuiltins.is_sequenceBI());
        builtins.put("is_string", new MiscellaneousBuiltins.is_stringBI());
        builtins.put("is_time", new MiscellaneousBuiltins.is_dateOfTypeBI(TemplateDateModel.TIME));
        builtins.put("is_transform", new MiscellaneousBuiltins.is_transformBI());
        
        builtins.put("iso_utc", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_SECONDS, /* useUTC = */ true));
        builtins.put("iso_utc_fz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.TRUE, DateUtil.ACCURACY_SECONDS, /* useUTC = */ true));
        builtins.put("iso_utc_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_SECONDS, /* useUTC = */ true));
        
        builtins.put("iso_utc_ms", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MILLISECONDS, /* useUTC = */ true));
        builtins.put("iso_utc_ms_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MILLISECONDS, /* useUTC = */ true));
        
        builtins.put("iso_utc_m", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MINUTES, /* useUTC = */ true));
        builtins.put("iso_utc_m_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MINUTES, /* useUTC = */ true));
        
        builtins.put("iso_utc_h", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_HOURS, /* useUTC = */ true));
        builtins.put("iso_utc_h_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_HOURS, /* useUTC = */ true));
        
        builtins.put("iso_local", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_SECONDS, /* useUTC = */ false));
        builtins.put("iso_local_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_SECONDS, /* useUTC = */ false));
        
        builtins.put("iso_local_ms", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MILLISECONDS, /* useUTC = */ false));
        builtins.put("iso_local_ms_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MILLISECONDS, /* useUTC = */ false));
        
        builtins.put("iso_local_m", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MINUTES, /* useUTC = */ false));
        builtins.put("iso_local_m_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MINUTES, /* useUTC = */ false));
        
        builtins.put("iso_local_h", new iso_utc_or_local_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_HOURS, /* useUTC = */ false));
        builtins.put("iso_local_h_nz", new iso_utc_or_local_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_HOURS, /* useUTC = */ false));
        
        builtins.put("iso", new iso_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_SECONDS));
        builtins.put("iso_nz", new iso_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_SECONDS));
        
        builtins.put("iso_ms", new iso_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MILLISECONDS));
        builtins.put("iso_ms_nz", new iso_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MILLISECONDS));
        
        builtins.put("iso_m", new iso_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_MINUTES));
        builtins.put("iso_m_nz", new iso_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_MINUTES));
        
        builtins.put("iso_h", new iso_BI(
                /* showOffset = */ null, DateUtil.ACCURACY_HOURS));
        builtins.put("iso_h_nz", new iso_BI(
                /* showOffset = */ Boolean.FALSE, DateUtil.ACCURACY_HOURS));
        
        builtins.put("j_string", new j_stringBI());
        builtins.put("join", new SequenceBuiltins.joinBI());
        builtins.put("js_string", new js_stringBI());
        builtins.put("json_string", new json_stringBI());
        builtins.put("keys", new HashBuiltins.keysBI());
        builtins.put("last_index_of", new StringBuiltins.index_ofBI(true));
        builtins.put("last", new lastBI());
        builtins.put("left_pad", new StringBuiltins.padBI(true));
        builtins.put("length", new StringBuiltins.lengthBI());
        builtins.put("long", new longBI());
        builtins.put("lower_case", new lower_caseBI());
        builtins.put("namespace", new MiscellaneousBuiltins.namespaceBI());
        builtins.put("new", new NewBI());
        builtins.put("node_name", new node_nameBI());
        builtins.put("node_namespace", new node_namespaceBI());
        builtins.put("node_type", new node_typeBI());
        builtins.put("number", new numberBI());
        builtins.put("number_to_date", new number_to_dateBI(TemplateDateModel.DATE));
        builtins.put("number_to_time", new number_to_dateBI(TemplateDateModel.TIME));
        builtins.put("number_to_datetime", new number_to_dateBI(TemplateDateModel.DATETIME));
        builtins.put("parent", new parentBI());
        builtins.put("reverse", new reverseBI());
        builtins.put("right_pad", new StringBuiltins.padBI(false));
        builtins.put("root", new rootBI());
        builtins.put("round", new roundBI());
        builtins.put("rtf", new StringBuiltins.rtfBI());
        builtins.put("seq_contains", new seq_containsBI());
        builtins.put("seq_index_of", new seq_index_ofBI(1));
        builtins.put("seq_last_index_of", new seq_index_ofBI(-1));
        builtins.put("short", new shortBI());
        builtins.put("size", new MiscellaneousBuiltins.sizeBI());
        builtins.put("sort_by", new sort_byBI());
        builtins.put("sort", new sortBI());
        builtins.put("starts_with", new StringBuiltins.starts_withBI());
        builtins.put("string", new MiscellaneousBuiltins.stringBI());
        builtins.put("substring", new substringBI());
        builtins.put("time", new MiscellaneousBuiltins.dateBI(TemplateDateModel.TIME));
        builtins.put("time_if_unknown", new DateBuiltins.dateType_if_unknownBI(TemplateDateModel.TIME));
        builtins.put("trim", new StringBuiltins.trimBI());
        builtins.put("uncap_first", new uncap_firstBI());
        builtins.put("upper_case", new upper_caseBI());
        builtins.put("url", new StringBuiltins.urlBI());
        builtins.put("url_path", new StringBuiltins.urlPathBI());
        builtins.put("values", new HashBuiltins.valuesBI());
        builtins.put("web_safe", builtins.get("html"));  // deprecated; use ?html instead
        builtins.put("word_list", new word_listBI());
        builtins.put("xhtml", new StringBuiltins.xhtmlBI());
        builtins.put("xml", new StringBuiltins.xmlBI());
        builtins.put("matches", new RegexBuiltins.matchesBI());
        builtins.put("groups", new RegexBuiltins.groupsBI());
        builtins.put("replace", new RegexBuiltins.replace_reBI());
        builtins.put("split", new RegexBuiltins.split_reBI());
    }

    static BuiltIn newBuiltIn(int incompatibleImprovements, Expression target, String key) throws ParseException {
        BuiltIn bi = (BuiltIn) builtins.get(key);
        if (bi == null) {
            StringBuffer buf = new StringBuffer(
                    "Unknown built-in: " + StringUtil.jQuote(key) + ". "
                    + "Help (latest version): http://freemarker.org/docs/ref_builtins.html; "
                    + "you're using FreeMarker " + Configuration.getVersion() + ".\n" 
                    + "The alphabetical list of built-ins:");
            List names = new ArrayList(builtins.keySet().size());
            names.addAll(builtins.keySet());
            Collections.sort(names);
            char lastLetter = 0;
            for (Iterator it = names.iterator(); it.hasNext();) {
                String name = (String) it.next();
                char firstChar = name.charAt(0);
                if (firstChar != lastLetter) {
                    lastLetter = firstChar;
                    buf.append('\n');
                }
                buf.append(name);
                
                if (it.hasNext()) {
                    buf.append(", ");
                }
            }
            throw new ParseException(buf.toString(), target);
        }
        
        while (bi instanceof ICIChainMember
                && incompatibleImprovements < ((ICIChainMember) bi).getMinimumICIVersion()) {
            bi = (BuiltIn) ((ICIChainMember) bi).getPreviousICIChainMember();
        }
        
        try {
            bi = (BuiltIn) bi.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        bi.target = target;
        bi.key = key;
        return bi;
    }

    public String getCanonicalForm() {
        return target.getCanonicalForm() + getNodeTypeSymbol();
    }
    
    String getNodeTypeSymbol() {
        return "?" + key;
    }

    boolean isLiteral() {
        return false; // be on the safe side.
    }
    
    protected final void checkMethodArgCount(List args, int expectedCnt) throws TemplateModelException {
        checkMethodArgCount(args.size(), expectedCnt);
    }
    
    protected final void checkMethodArgCount(int argCnt, int expectedCnt) throws TemplateModelException {
        if (argCnt != expectedCnt) {
            throw MessageUtil.newArgCntError("?" + key, argCnt, expectedCnt);
        }
    }

    protected final void checkMethodArgCount(List args, int minCnt, int maxCnt) throws TemplateModelException {
        checkMethodArgCount(args.size(), minCnt, maxCnt);
    }
    
    protected final void checkMethodArgCount(int argCnt, int minCnt, int maxCnt) throws TemplateModelException {
        if (argCnt < minCnt || argCnt > maxCnt) {
            throw MessageUtil.newArgCntError("?" + key, argCnt, minCnt, maxCnt);
        }
    }

    /**
     * Same as {@link #getStringMethodArg}, but checks if {@code args} is big enough, and returns {@code null} if it
     * isn't.
     */
    protected final String getOptStringMethodArg(List args, int argIdx)
            throws TemplateModelException {
        return args.size() > argIdx ? getStringMethodArg(args, argIdx) : null;
    }
    
    /**
     * Gets a method argument and checks if it's a string; it does NOT check if {@code args} is big enough.
     */
    protected final String getStringMethodArg(List args, int argIdx)
            throws TemplateModelException {
        TemplateModel arg = (TemplateModel) args.get(argIdx);
        if (!(arg instanceof TemplateScalarModel)) {
            throw MessageUtil.newMethodArgMustBeStringException("?" + key, argIdx, arg);
        } else {
            return EvalUtil.modelToString((TemplateScalarModel) arg, null, null);
        }
    }

    /**
     * Gets a method argument and checks if it's a number; it does NOT check if {@code args} is big enough.
     */
    protected final Number getNumberMethodArg(List args, int argIdx)
            throws TemplateModelException {
        TemplateModel arg = (TemplateModel) args.get(argIdx);
        if (!(arg instanceof TemplateNumberModel)) {
            throw MessageUtil.newMethodArgMustBeNumberException("?" + key, argIdx, arg);
        } else {
            return EvalUtil.modelToNumber((TemplateNumberModel) arg, null);
        }
    }
    
    protected final Expression deepCloneWithIdentifierReplaced_inner(
            String replacedIdentifier, Expression replacement, ReplacemenetState replacementState) {
    	try {
	    	BuiltIn clone = (BuiltIn)clone();
	    	clone.target = target.deepCloneWithIdentifierReplaced(replacedIdentifier, replacement, replacementState);
	    	return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("Internal error: " + e);
        }
    }

    int getParameterCount() {
        return 2;
    }

    Object getParameterValue(int idx) {
        switch (idx) {
        case 0: return target;
        case 1: return key;
        default: throw new IndexOutOfBoundsException();
        }
    }

    ParameterRole getParameterRole(int idx) {
        switch (idx) {
        case 0: return ParameterRole.LEFT_HAND_OPERAND;
        case 1: return ParameterRole.RIGHT_HAND_OPERAND;
        default: throw new IndexOutOfBoundsException();
        }
    }
    
}
