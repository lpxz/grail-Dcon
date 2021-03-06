///* Generated By:JavaCC: Do not edit this line. PNparser.java */
//package hk.ust.lpxz.petri.graph.GadaraSupport.parser;
//import hk.ust.lpxz.petri.graph.GadaraSupport.CommGadaraFormat;
//import java.io.StringReader;
//import java.io.Reader;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.HashMap;
//import java.util.HashSet;
//
//public class PNparser implements PNparserConstants {
//
//
//   public static List list ;
//    public PNparser(String s)
//    {
//        this((Reader)(new StringReader(s)));
//        list = new  ArrayList();
//    }
//
//
//    public static void main(String args[])
//    {
//        try
//        {       PNparser parser = new PNparser(new java.io.FileInputStream(args[0]));
//                parser.parse();
//                System.out.println(list.size());
//        }
//        catch(Exception e)
//        {
//                e.printStackTrace();
//        }
//    }
//
///** 
// *  Top level
// */
//  static final public void parse() throws ParseException {
//    whole();
//    jj_consume_token(0);
//  }
//
///**
// * An expression is defined to be a queryTerm followed by zero or more
// * query terms joined by either an AND or an OR.   If two query terms are joined with 
// * AND then both conditions must be met.  If two query terms are joined with an OR, then
// * one of the two conditions must be met.  
// */
//  static final public void whole() throws ParseException {
//    varDecList();
//    linkDecList();
//  }
//
//  static final public void varDecList() throws ParseException {
//    label_1:
//    while (true) {
//      varDec();
//      jj_consume_token(DECLSEP);
//      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//      case PLACECOMMON:
//      case PLACEENTRY:
//      case PLACEEXIT:
//      case PLACERESOURCE:
//      case PLACECONTROL:
//      case TRANSITION:
//      case ARCLOCAL:
//      case ARCCALL:
//      case ARCRETURN:
//      case ARCFROMRESOURCE:
//      case ARCTORESOURCE:
//      case ARCFROMCONTROLLER:
//      case ARCTOCONTROLLER:
//      case PETRIMETHOD:
//      case PETRICS:
//      case VIOLATION:
//        ;
//        break;
//      default:
//        jj_la1[0] = jj_gen;
//        break label_1;
//      }
//    }
//  }
//
//  static final public void linkDecList() throws ParseException {
//    label_2:
//    while (true) {
//      linkDec();
//      jj_consume_token(DECLSEP);
//      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//      case VARNAME:
//        ;
//        break;
//      default:
//        jj_la1[1] = jj_gen;
//        break label_2;
//      }
//    }
//  }
//
//  static final public void varDec() throws ParseException {
//  String vtype;
//  Token tvarname;
//  HashMap attriTable;
//    vtype = vartype();
//    tvarname = jj_consume_token(VARNAME);
//    jj_consume_token(LBRACE);
//    attriTable = attrilist();
//    jj_consume_token(RBRACE);
//     CommGadaraFormat.seeVarDec(vtype, tvarname.image, attriTable);
//  }
//
//  static final public String vartype() throws ParseException {
//  Token t;
//    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//    case PLACECOMMON:
//      t = jj_consume_token(PLACECOMMON);
//   {if (true) return t.image;}
//      break;
//    case PLACEENTRY:
//      t = jj_consume_token(PLACEENTRY);
//     {if (true) return t.image;}
//      break;
//    case PLACEEXIT:
//      t = jj_consume_token(PLACEEXIT);
//{if (true) return t.image;}
//      break;
//    case PLACERESOURCE:
//      t = jj_consume_token(PLACERESOURCE);
//{if (true) return t.image;}
//      break;
//    case PLACECONTROL:
//      t = jj_consume_token(PLACECONTROL);
//{if (true) return t.image;}
//      break;
//    case TRANSITION:
//      t = jj_consume_token(TRANSITION);
//{if (true) return t.image;}
//      break;
//    case ARCLOCAL:
//      t = jj_consume_token(ARCLOCAL);
//  {if (true) return t.image;}
//      break;
//    case ARCCALL:
//      t = jj_consume_token(ARCCALL);
//  {if (true) return t.image;}
//      break;
//    case ARCRETURN:
//      t = jj_consume_token(ARCRETURN);
//  {if (true) return t.image;}
//      break;
//    case ARCFROMRESOURCE:
//      t = jj_consume_token(ARCFROMRESOURCE);
//  {if (true) return t.image;}
//      break;
//    case ARCTORESOURCE:
//      t = jj_consume_token(ARCTORESOURCE);
//  {if (true) return t.image;}
//      break;
//    case ARCFROMCONTROLLER:
//      t = jj_consume_token(ARCFROMCONTROLLER);
//  {if (true) return t.image;}
//      break;
//    case ARCTOCONTROLLER:
//      t = jj_consume_token(ARCTOCONTROLLER);
//  {if (true) return t.image;}
//      break;
//    case PETRIMETHOD:
//      t = jj_consume_token(PETRIMETHOD);
//  {if (true) return t.image;}
//      break;
//    case PETRICS:
//      t = jj_consume_token(PETRICS);
//  {if (true) return t.image;}
//      break;
//    case VIOLATION:
//      t = jj_consume_token(VIOLATION);
//  {if (true) return t.image;}
//      break;
//    default:
//      jj_la1[2] = jj_gen;
//      jj_consume_token(-1);
//      throw new ParseException();
//    }
//    throw new Error("Missing return statement in function");
//  }
//
//  static final public HashMap attrilist() throws ParseException {
//  HashMap toret;
//  HashMap tmp;
//      toret = new HashMap();
//    tmp = attriDec();
//     toret.putAll(tmp);
//    label_3:
//    while (true) {
//      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//      case ATTRSEP:
//        ;
//        break;
//      default:
//        jj_la1[3] = jj_gen;
//        break label_3;
//      }
//      jj_consume_token(ATTRSEP);
//      tmp = attriDec();
//     toret.putAll(tmp);
//    }
//   {if (true) return toret;}
//    throw new Error("Missing return statement in function");
//  }
//
//  static final public HashMap attriDec() throws ParseException {
//   HashMap toret = new HashMap();
//   Token t1;
//   Token t2;
//    Token t3;
//    HashSet tmpSet;
//    if (jj_2_1(3)) {
//      t1 = jj_consume_token(STMTATTR);
//      jj_consume_token(EQUALS);
//      t3 = jj_consume_token(INTEGER);
//      toret.put(t1.image, t3.image); {if (true) return toret;}
//    } else if (jj_2_2(3)) {
//      t1 = jj_consume_token(STMTATTR);
//      jj_consume_token(EQUALS);
//      toret.put(t1.image, "");
//      {if (true) return toret;}
//    } else {
//      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//      case TOKENATTR:
//        t1 = jj_consume_token(TOKENATTR);
//        jj_consume_token(EQUALS);
//        t3 = jj_consume_token(INTEGER);
//      toret.put(t1.image, t3.image); {if (true) return toret;}
//        break;
//      default:
//        jj_la1[4] = jj_gen;
//        if (jj_2_3(3)) {
//          t1 = jj_consume_token(ENCLOSINGMATTR);
//          jj_consume_token(EQUALS);
//          t3 = jj_consume_token(VARNAME);
//      toret.put(t1.image, t3.image); {if (true) return toret;}
//        } else if (jj_2_4(3)) {
//          t1 = jj_consume_token(ENCLOSINGMATTR);
//          jj_consume_token(EQUALS);
//  toret.put(t1.image,""); {if (true) return toret;}
//        } else if (jj_2_5(3)) {
//          t1 = jj_consume_token(CONTROLLABLEATTR);
//          jj_consume_token(EQUALS);
//          t3 = jj_consume_token(TRUE);
//    toret.put(t1.image, t3.image); {if (true) return toret;}
//        } else if (jj_2_6(3)) {
//          t1 = jj_consume_token(CONTROLLABLEATTR);
//          jj_consume_token(EQUALS);
//          t3 = jj_consume_token(FALSE);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//        } else if (jj_2_7(3)) {
//          t1 = jj_consume_token(OBSERVABLEATTR);
//          jj_consume_token(EQUALS);
//          t3 = jj_consume_token(TRUE);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//        } else if (jj_2_8(3)) {
//          t1 = jj_consume_token(OBSERVABLEATTR);
//          jj_consume_token(EQUALS);
//          t3 = jj_consume_token(FALSE);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//        } else {
//          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//          case WEIGHTATTR:
//            t1 = jj_consume_token(WEIGHTATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(INTEGER);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case METHODSIGATTR:
//            t1 = jj_consume_token(METHODSIGATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(INTEGER);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case ENTRYATTR:
//            t1 = jj_consume_token(ENTRYATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case EXITATTR:
//            t1 = jj_consume_token(EXITATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case PPLACEATTR:
//            t1 = jj_consume_token(PPLACEATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case CPLACEATTR:
//            t1 = jj_consume_token(CPLACEATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case RPLACEATTR:
//            t1 = jj_consume_token(RPLACEATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case CTREDPLACE:
//            t1 = jj_consume_token(CTREDPLACE);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
// toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case RESOURCEATTR:
//            t1 = jj_consume_token(RESOURCEATTR);
//            jj_consume_token(EQUALS);
//            t3 = jj_consume_token(VARNAME);
//  toret.put(t1.image, t3.image); {if (true) return toret;}
//            break;
//          case OBSEDPLACES:
//            t1 = jj_consume_token(OBSEDPLACES);
//            jj_consume_token(EQUALS);
//            jj_consume_token(LBRACE);
//            tmpSet = varbag();
//            jj_consume_token(RBRACE);
//toret.put(t1.image, tmpSet); {if (true) return toret;}
//            break;
//          case PLACESATTR:
//            t1 = jj_consume_token(PLACESATTR);
//            jj_consume_token(EQUALS);
//            jj_consume_token(LBRACE);
//            tmpSet = varbag();
//            jj_consume_token(RBRACE);
//toret.put(t1.image, tmpSet); {if (true) return toret;}
//            break;
//          default:
//            jj_la1[5] = jj_gen;
//            jj_consume_token(-1);
//            throw new ParseException();
//          }
//        }
//      }
//    }
//    throw new Error("Missing return statement in function");
//  }
//
//  static final public HashSet varbag() throws ParseException {
//  HashSet toret;
//  Token t1;
//  Token t2;
//toret = new HashSet();
//    t1 = jj_consume_token(VARNAME);
//    toret.add(t1.image);
//    label_4:
//    while (true) {
//      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
//      case ATTRSEP:
//        ;
//        break;
//      default:
//        jj_la1[6] = jj_gen;
//        break label_4;
//      }
//      jj_consume_token(ATTRSEP);
//      t2 = jj_consume_token(VARNAME);
//toret.add(t2.image);
//    }
//  {if (true) return toret;}
//    throw new Error("Missing return statement in function");
//  }
//
//  static final public void linkDec() throws ParseException {
// Token t1;
//Token t2;
//Token t3;
//    t1 = jj_consume_token(VARNAME);
//    jj_consume_token(ARROW);
//    t2 = jj_consume_token(VARNAME);
//    jj_consume_token(ARROW);
//    t3 = jj_consume_token(VARNAME);
//   CommGadaraFormat.seelinkDec(t1.image, t2.image, t3.image);
//  }
//
//  static final private boolean jj_2_1(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_1(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(0, xla); }
//  }
//
//  static final private boolean jj_2_2(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_2(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(1, xla); }
//  }
//
//  static final private boolean jj_2_3(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_3(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(2, xla); }
//  }
//
//  static final private boolean jj_2_4(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_4(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(3, xla); }
//  }
//
//  static final private boolean jj_2_5(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_5(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(4, xla); }
//  }
//
//  static final private boolean jj_2_6(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_6(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(5, xla); }
//  }
//
//  static final private boolean jj_2_7(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_7(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(6, xla); }
//  }
//
//  static final private boolean jj_2_8(int xla) {
//    jj_la = xla; jj_lastpos = jj_scanpos = token;
//    try { return !jj_3_8(); }
//    catch(LookaheadSuccess ls) { return true; }
//    finally { jj_save(7, xla); }
//  }
//
//  static final private boolean jj_3_8() {
//    if (jj_scan_token(OBSERVABLEATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(FALSE)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_3() {
//    if (jj_scan_token(ENCLOSINGMATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(VARNAME)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_2() {
//    if (jj_scan_token(STMTATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_5() {
//    if (jj_scan_token(CONTROLLABLEATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(TRUE)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_4() {
//    if (jj_scan_token(ENCLOSINGMATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_1() {
//    if (jj_scan_token(STMTATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(INTEGER)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_7() {
//    if (jj_scan_token(OBSERVABLEATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(TRUE)) return true;
//    return false;
//  }
//
//  static final private boolean jj_3_6() {
//    if (jj_scan_token(CONTROLLABLEATTR)) return true;
//    if (jj_scan_token(EQUALS)) return true;
//    if (jj_scan_token(FALSE)) return true;
//    return false;
//  }
//
//  static private boolean jj_initialized_once = false;
//  static public PNparserTokenManager token_source;
//  static SimpleCharStream jj_input_stream;
//  static public Token token, jj_nt;
//  static private int jj_ntk;
//  static private Token jj_scanpos, jj_lastpos;
//  static private int jj_la;
//  static public boolean lookingAhead = false;
//  static private boolean jj_semLA;
//  static private int jj_gen;
//  static final private int[] jj_la1 = new int[7];
//  static private int[] jj_la1_0;
//  static private int[] jj_la1_1;
//  static {
//      jj_la1_0();
//      jj_la1_1();
//   }
//   private static void jj_la1_0() {
//      jj_la1_0 = new int[] {0x3fffc00,0x0,0x3fffc00,0x40,0x10000000,0x0,0x40,};
//   }
//   private static void jj_la1_1() {
//      jj_la1_1 = new int[] {0x0,0x2000,0x0,0x0,0x0,0x7ff,0x0,};
//   }
//  static final private JJCalls[] jj_2_rtns = new JJCalls[8];
//  static private boolean jj_rescan = false;
//  static private int jj_gc = 0;
//
//  public PNparser(java.io.InputStream stream) {
//     this(stream, null);
//  }
//  public PNparser(java.io.InputStream stream, String encoding) {
//    if (jj_initialized_once) {
//      System.out.println("ERROR: Second call to constructor of static parser.  You must");
//      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
//      System.out.println("       during parser generation.");
//      throw new Error();
//    }
//    jj_initialized_once = true;
//    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
//    token_source = new PNparserTokenManager(jj_input_stream);
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  static public void ReInit(java.io.InputStream stream) {
//     ReInit(stream, null);
//  }
//  static public void ReInit(java.io.InputStream stream, String encoding) {
//    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
//    token_source.ReInit(jj_input_stream);
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  public PNparser(java.io.Reader stream) {
//    if (jj_initialized_once) {
//      System.out.println("ERROR: Second call to constructor of static parser.  You must");
//      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
//      System.out.println("       during parser generation.");
//      throw new Error();
//    }
//    jj_initialized_once = true;
//    jj_input_stream = new SimpleCharStream(stream, 1, 1);
//    token_source = new PNparserTokenManager(jj_input_stream);
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  static public void ReInit(java.io.Reader stream) {
//    jj_input_stream.ReInit(stream, 1, 1);
//    token_source.ReInit(jj_input_stream);
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  public PNparser(PNparserTokenManager tm) {
//    if (jj_initialized_once) {
//      System.out.println("ERROR: Second call to constructor of static parser.  You must");
//      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
//      System.out.println("       during parser generation.");
//      throw new Error();
//    }
//    jj_initialized_once = true;
//    token_source = tm;
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  public void ReInit(PNparserTokenManager tm) {
//    token_source = tm;
//    token = new Token();
//    jj_ntk = -1;
//    jj_gen = 0;
//    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
//    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
//  }
//
//  static final private Token jj_consume_token(int kind) throws ParseException {
//    Token oldToken;
//    if ((oldToken = token).next != null) token = token.next;
//    else token = token.next = token_source.getNextToken();
//    jj_ntk = -1;
//    if (token.kind == kind) {
//      jj_gen++;
//      if (++jj_gc > 100) {
//        jj_gc = 0;
//        for (int i = 0; i < jj_2_rtns.length; i++) {
//          JJCalls c = jj_2_rtns[i];
//          while (c != null) {
//            if (c.gen < jj_gen) c.first = null;
//            c = c.next;
//          }
//        }
//      }
//      return token;
//    }
//    token = oldToken;
//    jj_kind = kind;
//    throw generateParseException();
//  }
//
//  static private final class LookaheadSuccess extends java.lang.Error { }
//  static final private LookaheadSuccess jj_ls = new LookaheadSuccess();
//  static final private boolean jj_scan_token(int kind) {
//    if (jj_scanpos == jj_lastpos) {
//      jj_la--;
//      if (jj_scanpos.next == null) {
//        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
//      } else {
//        jj_lastpos = jj_scanpos = jj_scanpos.next;
//      }
//    } else {
//      jj_scanpos = jj_scanpos.next;
//    }
//    if (jj_rescan) {
//      int i = 0; Token tok = token;
//      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
//      if (tok != null) jj_add_error_token(kind, i);
//    }
//    if (jj_scanpos.kind != kind) return true;
//    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
//    return false;
//  }
//
//  static final public Token getNextToken() {
//    if (token.next != null) token = token.next;
//    else token = token.next = token_source.getNextToken();
//    jj_ntk = -1;
//    jj_gen++;
//    return token;
//  }
//
//  static final public Token getToken(int index) {
//    Token t = lookingAhead ? jj_scanpos : token;
//    for (int i = 0; i < index; i++) {
//      if (t.next != null) t = t.next;
//      else t = t.next = token_source.getNextToken();
//    }
//    return t;
//  }
//
//  static final private int jj_ntk() {
//    if ((jj_nt=token.next) == null)
//      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
//    else
//      return (jj_ntk = jj_nt.kind);
//  }
//
//  static private java.util.Vector jj_expentries = new java.util.Vector();
//  static private int[] jj_expentry;
//  static private int jj_kind = -1;
//  static private int[] jj_lasttokens = new int[100];
//  static private int jj_endpos;
//
//  static private void jj_add_error_token(int kind, int pos) {
//    if (pos >= 100) return;
//    if (pos == jj_endpos + 1) {
//      jj_lasttokens[jj_endpos++] = kind;
//    } else if (jj_endpos != 0) {
//      jj_expentry = new int[jj_endpos];
//      for (int i = 0; i < jj_endpos; i++) {
//        jj_expentry[i] = jj_lasttokens[i];
//      }
//      boolean exists = false;
//      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
//        int[] oldentry = (int[])(e.nextElement());
//        if (oldentry.length == jj_expentry.length) {
//          exists = true;
//          for (int i = 0; i < jj_expentry.length; i++) {
//            if (oldentry[i] != jj_expentry[i]) {
//              exists = false;
//              break;
//            }
//          }
//          if (exists) break;
//        }
//      }
//      if (!exists) jj_expentries.addElement(jj_expentry);
//      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
//    }
//  }
//
//  static public ParseException generateParseException() {
//    jj_expentries.removeAllElements();
//    boolean[] la1tokens = new boolean[47];
//    for (int i = 0; i < 47; i++) {
//      la1tokens[i] = false;
//    }
//    if (jj_kind >= 0) {
//      la1tokens[jj_kind] = true;
//      jj_kind = -1;
//    }
//    for (int i = 0; i < 7; i++) {
//      if (jj_la1[i] == jj_gen) {
//        for (int j = 0; j < 32; j++) {
//          if ((jj_la1_0[i] & (1<<j)) != 0) {
//            la1tokens[j] = true;
//          }
//          if ((jj_la1_1[i] & (1<<j)) != 0) {
//            la1tokens[32+j] = true;
//          }
//        }
//      }
//    }
//    for (int i = 0; i < 47; i++) {
//      if (la1tokens[i]) {
//        jj_expentry = new int[1];
//        jj_expentry[0] = i;
//        jj_expentries.addElement(jj_expentry);
//      }
//    }
//    jj_endpos = 0;
//    jj_rescan_token();
//    jj_add_error_token(0, 0);
//    int[][] exptokseq = new int[jj_expentries.size()][];
//    for (int i = 0; i < jj_expentries.size(); i++) {
//      exptokseq[i] = (int[])jj_expentries.elementAt(i);
//    }
//    return new ParseException(token, exptokseq, tokenImage);
//  }
//
//  static final public void enable_tracing() {
//  }
//
//  static final public void disable_tracing() {
//  }
//
//  static final private void jj_rescan_token() {
//    jj_rescan = true;
//    for (int i = 0; i < 8; i++) {
//    try {
//      JJCalls p = jj_2_rtns[i];
//      do {
//        if (p.gen > jj_gen) {
//          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
//          switch (i) {
//            case 0: jj_3_1(); break;
//            case 1: jj_3_2(); break;
//            case 2: jj_3_3(); break;
//            case 3: jj_3_4(); break;
//            case 4: jj_3_5(); break;
//            case 5: jj_3_6(); break;
//            case 6: jj_3_7(); break;
//            case 7: jj_3_8(); break;
//          }
//        }
//        p = p.next;
//      } while (p != null);
//      } catch(LookaheadSuccess ls) { }
//    }
//    jj_rescan = false;
//  }
//
//  static final private void jj_save(int index, int xla) {
//    JJCalls p = jj_2_rtns[index];
//    while (p.gen > jj_gen) {
//      if (p.next == null) { p = p.next = new JJCalls(); break; }
//      p = p.next;
//    }
//    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
//  }
//
//  static final class JJCalls {
//    int gen;
//    Token first;
//    int arg;
//    JJCalls next;
//  }
//
//}
