package org.example.benchmarkcode;

import org.strategoxt.stratego_lib.*;import org.strategoxt.lang.*;
import org.spoofax.interpreter.terms.*;import static org.strategoxt.lang.Term.*;

import java.lang.ref.WeakReference;
@SuppressWarnings("all")public class str2_example_xy_fused {
protected static final  boolean TRACES_ENABLED=true;
protected static  ITermFactory constantFactory;
private static  WeakReference<Context> initedContext;
private static  boolean isIniting;
protected static  IStrategoTerm constS0;
protected static  IStrategoTerm constZ0;
protected static  IStrategoTerm constNull0;
protected static  IStrategoTerm const0;
public static  IStrategoConstructor _consConc_2;
public static  IStrategoConstructor _consNone_0;
public static  IStrategoConstructor _consSome_1;
public static  IStrategoConstructor _consMult_2;
public static  IStrategoConstructor _consPlus_2;
public static  IStrategoConstructor _consE_5;
public static  IStrategoConstructor _consNull_0;
public static  IStrategoConstructor _consS_1;
public static  IStrategoConstructor _consZ_0;
public static Context init(Context context){
synchronized(str2_example_xy_fused.class){
if(isIniting)return null;
try{
isIniting=true;
ITermFactory termFactory=context.getFactory();
if(constantFactory==null){
initConstructors(termFactory);
initConstants(termFactory);
}
if(initedContext==null||initedContext.get()!=context){
org.strategoxt.stratego_lib.Main.init(context);
context.registerComponent("example_xy_fused");
}
initedContext=new WeakReference<Context>(context);
constantFactory=termFactory;
}
finally{
isIniting=false;
}
return context;
}
}
    public static IStrategoTerm mainNoExit(String...args)throws StrategoExit{
        return mainNoExit(new Context(),args);
    }
    public static IStrategoTerm mainNoExit(Context context,String...args)throws StrategoExit{
        try{
            init(context);
            return context.invokeStrategyCLI(str2_example_xy_fused.main_0_0.instance,"example_xy_fused",args);
        }
        finally{
            context.getIOAgent().closeAllFiles();
        }
    }
public static Context init(){
return init(new Context());
}
public static Strategy getMainStrategy(){
return null;
}
public static void initConstructors(ITermFactory termFactory){
_consConc_2=termFactory.makeConstructor("Conc",2);
_consNone_0=termFactory.makeConstructor("None",0);
_consSome_1=termFactory.makeConstructor("Some",1);
_consMult_2=termFactory.makeConstructor("Mult",2);
_consPlus_2=termFactory.makeConstructor("Plus",2);
_consE_5=termFactory.makeConstructor("E",5);
_consNull_0=termFactory.makeConstructor("Null",0);
_consS_1=termFactory.makeConstructor("S",1);
_consZ_0=termFactory.makeConstructor("Z",0);
}
public static void initConstants(ITermFactory termFactory){
const0=termFactory.makeString("");
constNull0=termFactory.makeAppl(str2_example_xy_fused._consNull_0,NO_TERMS);
constZ0=termFactory.makeAppl(str2_example_xy_fused._consZ_0,NO_TERMS);
constS0=termFactory.makeAppl(str2_example_xy_fused._consS_1,new IStrategoTerm[]{str2_example_xy_fused.constZ0});
}
@SuppressWarnings("all")public static class f__main_0_0 extends Strategy{
public static final  f__main_0_0 instance=new f__main_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main_0_0");
Fail0:{
switch(term.getType()){
case APPL:{
IStrategoAppl appl_0=(IStrategoAppl)term;
if(appl_0.getConstructor()== str2_example_xy_fused._consNull_0){
term=f__main__$Null_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {
if(appl_0.getConstructor()== str2_example_xy_fused._consS_1){
term=f__main__$S_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {
if(appl_0.getConstructor()== str2_example_xy_fused._consMult_2){
term=f__main__$Mult_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {
if(appl_0.getConstructor()== str2_example_xy_fused._consE_5){
term=f__main__$E_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {
if(appl_0.getConstructor()== str2_example_xy_fused._consZ_0){
term=f__main__$Z_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {
if(appl_0.getConstructor()== str2_example_xy_fused._consPlus_2){
term=f__main__$Plus_0_0.instance.invoke(context,term);
if(term==null)break Fail0;
}
else {}
}
}
}
}
}
break;}
default:{
break;}
}
context.popOnSuccess("f__main_0_0");
if(true)return term;
}
context.popOnFailure("f__main_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$Null_0_0 extends Strategy{
public static final  f__main__$Null_0_0 instance=new f__main__$Null_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__Null_0_0");
Fail1:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail1;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail1;
context.popOnSuccess("f__main__Null_0_0");
if(true)return term;
}
context.popOnFailure("f__main__Null_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$S_0_0 extends Strategy{
public static final  f__main__$S_0_0 instance=new f__main__$S_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__S_0_0");
Fail2:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail2;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail2;
context.popOnSuccess("f__main__S_0_0");
if(true)return term;
}
context.popOnFailure("f__main__S_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$Mult_0_0 extends Strategy{
public static final  f__main__$Mult_0_0 instance=new f__main__$Mult_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__Mult_0_0");
Fail3:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail3;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail3;
context.popOnSuccess("f__main__Mult_0_0");
if(true)return term;
}
context.popOnFailure("f__main__Mult_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$E_0_0 extends Strategy{
public static final  f__main__$E_0_0 instance=new f__main__$E_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__E_0_0");
Fail4:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail4;
term=$E_5_0.instance.invoke(context,term,f__main_0_0.instance,f__main_0_0.instance,_Id.instance,_Id.instance,_Id.instance);
if(term==null)break Fail4;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail4;
context.popOnSuccess("f__main__E_0_0");
if(true)return term;
}
context.popOnFailure("f__main__E_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$Z_0_0 extends Strategy{
public static final  f__main__$Z_0_0 instance=new f__main__$Z_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__Z_0_0");
Fail5:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail5;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail5;
context.popOnSuccess("f__main__Z_0_0");
if(true)return term;
}
context.popOnFailure("f__main__Z_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class f__main__$Plus_0_0 extends Strategy{
public static final  f__main__$Plus_0_0 instance=new f__main__$Plus_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("f__main__Plus_0_0");
Fail6:{
term=r2_0_0.instance.invoke(context,term);
if(term==null)break Fail6;
term=r1_0_0.instance.invoke(context,term);
if(term==null)break Fail6;
context.popOnSuccess("f__main__Plus_0_0");
if(true)return term;
}
context.popOnFailure("f__main__Plus_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class main_0_0 extends Strategy{
public static final  main_0_0 instance=new main_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
context.push("main_0_0");
Fail7:{
IStrategoTerm main_0_0_list1=null;
TermReference main_0_0_filename1=new TermReference();
IStrategoTerm main_0_0_inp1=null;
IStrategoTerm main_0_0_result1=null;
IStrategoTerm main_0_0_resultt1=null;
IStrategoTerm main_0_0_where2=null;
IStrategoTerm main_0_0_lift_app_in_build_m1=null;
IStrategoTerm main_0_0_where3=null;
main_0_0_list1=term;
main_0_0_where2=term;
if(term.getTermType()!=IStrategoTerm.LIST||((IStrategoList)term).isEmpty())break Fail7;
IStrategoTerm arg1=((IStrategoList)term).tail();
if(arg1.getTermType()!=IStrategoTerm.LIST||((IStrategoList)arg1).isEmpty())break Fail7;
if(main_0_0_filename1.value==null)main_0_0_filename1.value=((IStrategoList)arg1).head();
else if(main_0_0_filename1.value!=((IStrategoList)arg1).head()&&!main_0_0_filename1.value.match(((IStrategoList)arg1).head()))break Fail7;
main_0_0_where3=term;
main_0_0_lifted0 main_0_0_lifted00=new main_0_0_lifted0();
main_0_0_lifted00.main_0_0_filename1=main_0_0_filename1;
term=open_1_0.instance.invoke(context,term,main_0_0_lifted00);
if(term==null)break Fail7;
main_0_0_lift_app_in_build_m1=term;
main_0_0_inp1=term;
term=f__main_0_0.instance.invoke(context,term);
if(term==null)break Fail7;
main_0_0_result1=term;
term= str2_example_xy_fused.const0;
main_0_0_resultt1= str2_example_xy_fused.const0;
term= str2_example_xy_fused.const0;
context.popOnSuccess("main_0_0");
if(true)return term;
}
context.popOnFailure("main_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class r1_0_0 extends Strategy{
public static final  r1_0_0 instance=new r1_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
Fail8:{
IStrategoTerm r1_0_0_l1=null;
IStrategoTerm r1_0_0_r1=null;
IStrategoTerm r1_0_0_v1=null;
IStrategoTerm r1_0_0_x1=null;
IStrategoTerm r1_0_0_y1=null;
IStrategoTerm r1_0_0_n1=null;
IStrategoTerm r1_0_0_a2=null;
IStrategoTerm r1_0_0_b2=null;
IStrategoTerm r1_0_0_a3=null;
IStrategoTerm r1_0_0_b3=null;
switch(term.getType()){
case APPL:{
IStrategoAppl appl_1=(IStrategoAppl)term;
if(appl_1.getConstructor()== str2_example_xy_fused._consE_5){
IStrategoTerm term_3=term;
Success0:{
Fail9:{
IStrategoTerm where0=null;
IStrategoTerm where1=null;
IStrategoTerm where2=null;
IStrategoTerm where3=null;
IStrategoTerm where4=null;
where0=term;
term=term.getSubterm(4);
r1_0_0_y1=term;
term=where0;
where1=where0;
term=term.getSubterm(3);
r1_0_0_x1=term;
term=where1;
where2=where1;
term=term.getSubterm(2);
r1_0_0_v1=term;
term=where2;
where3=where2;
term=term.getSubterm(1);
r1_0_0_r1=term;
term=where3;
where4=where3;
term=term.getSubterm(0);
r1_0_0_l1=term;
term=where4;
{
term=termFactory.makeAppl(str2_example_xy_fused._consE_5,new IStrategoTerm[]{r1_0_0_l1,r1_0_0_r1,termFactory.makeAppl(str2_example_xy_fused._consPlus_2,new IStrategoTerm[]{r1_0_0_v1, str2_example_xy_fused.constS0}),r1_0_0_x1,r1_0_0_y1});
if(true)break Success0;
}
}
term=term_3;
if(true)break Fail8;
}
}
else {
if(appl_1.getConstructor()== str2_example_xy_fused._consZ_0){
term= str2_example_xy_fused.constZ0;
}
else {
if(appl_1.getConstructor()== str2_example_xy_fused._consS_1){
IStrategoTerm term_2=term;
Success1:{
Fail10:{
IStrategoTerm where5=null;
where5=term;
term=term.getSubterm(0);
r1_0_0_n1=term;
term=where5;
{
term=termFactory.makeAppl(str2_example_xy_fused._consS_1,new IStrategoTerm[]{r1_0_0_n1});
if(true)break Success1;
}
}
term=term_2;
if(true)break Fail8;
}
}
else {
if(appl_1.getConstructor()== str2_example_xy_fused._consNull_0){
term= str2_example_xy_fused.constNull0;
}
else {
if(appl_1.getConstructor()== str2_example_xy_fused._consPlus_2){
IStrategoTerm term_1=term;
Success2:{
Fail11:{
IStrategoTerm where6=null;
IStrategoTerm where7=null;
where6=term;
term=term.getSubterm(1);
r1_0_0_b2=term;
term=where6;
where7=where6;
term=term.getSubterm(0);
r1_0_0_a2=term;
term=where7;
{
term=termFactory.makeAppl(str2_example_xy_fused._consPlus_2,new IStrategoTerm[]{r1_0_0_a2,r1_0_0_b2});
if(true)break Success2;
}
}
term=term_1;
if(true)break Fail8;
}
}
else {
if(appl_1.getConstructor()== str2_example_xy_fused._consMult_2){
IStrategoTerm term_0=term;
Success3:{
Fail12:{
IStrategoTerm where8=null;
IStrategoTerm where9=null;
where8=term;
term=term.getSubterm(1);
r1_0_0_b3=term;
term=where8;
where9=where8;
term=term.getSubterm(0);
r1_0_0_a3=term;
term=where9;
{
term=termFactory.makeAppl(str2_example_xy_fused._consMult_2,new IStrategoTerm[]{r1_0_0_a3,r1_0_0_b3});
if(true)break Success3;
}
}
term=term_0;
if(true)break Fail8;
}
}
else {
if(true)break Fail8;
}
}
}
}
}
}
break;}
default:{
if(true)break Fail8;
break;}
}
if(true)return term;
}
context.push("r1_0_0");
context.popOnFailure("r1_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")public static class r2_0_0 extends Strategy{
public static final  r2_0_0 instance=new r2_0_0();
public static IStrategoTerm callStatic(Context context,IStrategoTerm term){
ITermFactory termFactory=context.getFactory();
Fail13:{
IStrategoTerm r2_0_0_l1=null;
IStrategoTerm r2_0_0_r1=null;
IStrategoTerm r2_0_0_v1=null;
IStrategoTerm r2_0_0_x1=null;
IStrategoTerm r2_0_0_y1=null;
IStrategoTerm r2_0_0_n1=null;
IStrategoTerm r2_0_0_a2=null;
IStrategoTerm r2_0_0_b2=null;
IStrategoTerm r2_0_0_a3=null;
IStrategoTerm r2_0_0_b3=null;
switch(term.getType()){
case APPL:{
IStrategoAppl appl_2=(IStrategoAppl)term;
if(appl_2.getConstructor()== str2_example_xy_fused._consE_5){
IStrategoTerm term_7=term;
Success4:{
Fail14:{
IStrategoTerm where10=null;
IStrategoTerm where11=null;
IStrategoTerm where12=null;
IStrategoTerm where13=null;
IStrategoTerm where14=null;
where10=term;
term=term.getSubterm(4);
r2_0_0_y1=term;
term=where10;
where11=where10;
term=term.getSubterm(3);
r2_0_0_x1=term;
term=where11;
where12=where11;
term=term.getSubterm(2);
r2_0_0_v1=term;
term=where12;
where13=where12;
term=term.getSubterm(1);
r2_0_0_r1=term;
term=where13;
where14=where13;
term=term.getSubterm(0);
r2_0_0_l1=term;
term=where14;
{
term=termFactory.makeAppl(str2_example_xy_fused._consE_5,new IStrategoTerm[]{r2_0_0_l1,r2_0_0_r1,r2_0_0_v1,termFactory.makeAppl(str2_example_xy_fused._consPlus_2,new IStrategoTerm[]{r2_0_0_x1, str2_example_xy_fused.constS0}),r2_0_0_y1});
if(true)break Success4;
}
}
term=term_7;
if(true)break Fail13;
}
}
else {
if(appl_2.getConstructor()== str2_example_xy_fused._consZ_0){
term= str2_example_xy_fused.constZ0;
}
else {
if(appl_2.getConstructor()== str2_example_xy_fused._consS_1){
IStrategoTerm term_6=term;
Success5:{
Fail15:{
IStrategoTerm where15=null;
where15=term;
term=term.getSubterm(0);
r2_0_0_n1=term;
term=where15;
{
term=termFactory.makeAppl(str2_example_xy_fused._consS_1,new IStrategoTerm[]{r2_0_0_n1});
if(true)break Success5;
}
}
term=term_6;
if(true)break Fail13;
}
}
else {
if(appl_2.getConstructor()== str2_example_xy_fused._consNull_0){
term= str2_example_xy_fused.constNull0;
}
else {
if(appl_2.getConstructor()== str2_example_xy_fused._consPlus_2){
IStrategoTerm term_5=term;
Success6:{
Fail16:{
IStrategoTerm where16=null;
IStrategoTerm where17=null;
where16=term;
term=term.getSubterm(1);
r2_0_0_b2=term;
term=where16;
where17=where16;
term=term.getSubterm(0);
r2_0_0_a2=term;
term=where17;
{
term=termFactory.makeAppl(str2_example_xy_fused._consPlus_2,new IStrategoTerm[]{r2_0_0_a2,r2_0_0_b2});
if(true)break Success6;
}
}
term=term_5;
if(true)break Fail13;
}
}
else {
if(appl_2.getConstructor()== str2_example_xy_fused._consMult_2){
IStrategoTerm term_4=term;
Success7:{
Fail17:{
IStrategoTerm where18=null;
IStrategoTerm where19=null;
where18=term;
term=term.getSubterm(1);
r2_0_0_b3=term;
term=where18;
where19=where18;
term=term.getSubterm(0);
r2_0_0_a3=term;
term=where19;
{
term=termFactory.makeAppl(str2_example_xy_fused._consMult_2,new IStrategoTerm[]{r2_0_0_a3,r2_0_0_b3});
if(true)break Success7;
}
}
term=term_4;
if(true)break Fail13;
}
}
else {
if(true)break Fail13;
}
}
}
}
}
}
break;}
default:{
if(true)break Fail13;
break;}
}
if(true)return term;
}
context.push("r2_0_0");
context.popOnFailure("r2_0_0");
return null;
}
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
return callStatic(context,term);
}
}
@SuppressWarnings("all")private static final class main_0_0_lifted0 extends Strategy{
 TermReference main_0_0_filename1;
@Override public IStrategoTerm invoke(Context context,IStrategoTerm term){
Fail18:{
if(main_0_0_filename1.value==null)break Fail18;
term=main_0_0_filename1.value;
if(true)return term;
}
return null;
}
}
public static void registerInterop(org.spoofax.interpreter.core.IContext context,Context compiledContext){
new InteropRegisterer().registerLazy(context,compiledContext,InteropRegisterer.class.getClassLoader());
}
@SuppressWarnings("unused")public static class InteropRegisterer extends org.strategoxt.lang.InteropRegisterer{
@Override public void register(org.spoofax.interpreter.core.IContext context,Context compiledContext){
register(context,compiledContext,context.getVarScope());
}
@Override public void registerLazy(org.spoofax.interpreter.core.IContext context,Context compiledContext,ClassLoader classLoader){
registerLazy(context,compiledContext,classLoader,context.getVarScope());
}
private void register(org.spoofax.interpreter.core.IContext context,Context compiledContext,org.spoofax.interpreter.core.VarScope varScope){
compiledContext.registerComponent("example_xy_fused");
str2_example_xy_fused.init(compiledContext);
varScope.addSVar("f__main_0_0",new InteropSDefT(f__main_0_0.instance,context));
varScope.addSVar("f__main__Null_0_0",new InteropSDefT(f__main__$Null_0_0.instance,context));
varScope.addSVar("f__main__S_0_0",new InteropSDefT(f__main__$S_0_0.instance,context));
varScope.addSVar("f__main__Mult_0_0",new InteropSDefT(f__main__$Mult_0_0.instance,context));
varScope.addSVar("f__main__E_0_0",new InteropSDefT(f__main__$E_0_0.instance,context));
varScope.addSVar("f__main__Z_0_0",new InteropSDefT(f__main__$Z_0_0.instance,context));
varScope.addSVar("f__main__Plus_0_0",new InteropSDefT(f__main__$Plus_0_0.instance,context));
varScope.addSVar("main_0_0",new InteropSDefT(main_0_0.instance,context));
varScope.addSVar("r1_0_0",new InteropSDefT(r1_0_0.instance,context));
varScope.addSVar("r2_0_0",new InteropSDefT(r2_0_0.instance,context));
}
private void registerLazy(org.spoofax.interpreter.core.IContext context,Context compiledContext,ClassLoader classLoader,org.spoofax.interpreter.core.VarScope varScope){
compiledContext.registerComponent("example_xy_fused");
str2_example_xy_fused.init(compiledContext);
varScope.addSVar("f__main_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main_0_0",context));
varScope.addSVar("f__main__Null_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$Null_0_0",context));
varScope.addSVar("f__main__S_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$S_0_0",context));
varScope.addSVar("f__main__Mult_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$Mult_0_0",context));
varScope.addSVar("f__main__E_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$E_0_0",context));
varScope.addSVar("f__main__Z_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$Z_0_0",context));
varScope.addSVar("f__main__Plus_0_0",new InteropSDefT(classLoader,"example_xy_fused$f__main__$Plus_0_0",context));
varScope.addSVar("main_0_0",new InteropSDefT(classLoader,"example_xy_fused$main_0_0",context));
varScope.addSVar("r1_0_0",new InteropSDefT(classLoader,"example_xy_fused$r1_0_0",context));
varScope.addSVar("r2_0_0",new InteropSDefT(classLoader,"example_xy_fused$r2_0_0",context));
}
}
    public static class $S_1_0 extends Strategy{
        public static str2_example_xy_fused.$S_1_0 instance=new str2_example_xy_fused.$S_1_0();
        @Override public IStrategoTerm invoke(Context context,IStrategoTerm term,Strategy cong_arg_10){
            ITermFactory termFactory=context.getFactory();
            context.push("S_1_0");
            Fail52:{
                IStrategoTerm trans_cong1=null;
                IStrategoTerm trans_cong0=null;
                if(term.getTermType()!=IStrategoTerm.APPL||str2_example_xy_fused._consS_1!=((IStrategoAppl)term).getConstructor())break Fail52;
                trans_cong0=term.getSubterm(0);
                IStrategoList annos0=term.getAnnotations();
                trans_cong1=annos0;
                term=cong_arg_10.invoke(context,trans_cong0);
                if(term==null)break Fail52;
                term=termFactory.annotateTerm(termFactory.makeAppl(str2_example_xy_fused._consS_1,new IStrategoTerm[]{term}),checkListAnnos(termFactory,trans_cong1));
                context.popOnSuccess();
                if(true)return term;
            }
            context.popOnFailure();
            return null;
        }
    }
    @SuppressWarnings("all")public static class $E_5_0 extends Strategy{
        public static str2_example_xy_fused.$E_5_0 instance=new str2_example_xy_fused.$E_5_0();
        @Override public IStrategoTerm invoke(Context context,IStrategoTerm term,Strategy cong_arg_11,Strategy cong_arg_20,Strategy cong_arg_30,Strategy cong_arg_40,Strategy cong_arg_50){
            ITermFactory termFactory=context.getFactory();
            context.push("E_5_0");
            Fail53:{
                IStrategoTerm trans_cong8=null;
                IStrategoTerm trans_cong3=null;
                IStrategoTerm trans_cong4=null;
                IStrategoTerm trans_cong5=null;
                IStrategoTerm trans_cong6=null;
                IStrategoTerm trans_cong7=null;
                IStrategoTerm trans_cong9=null;
                IStrategoTerm trans_cong10=null;
                IStrategoTerm trans_cong11=null;
                IStrategoTerm trans_cong12=null;
                if(term.getTermType()!=IStrategoTerm.APPL||str2_example_xy_fused._consE_5!=((IStrategoAppl)term).getConstructor())break Fail53;
                trans_cong3=term.getSubterm(0);
                trans_cong4=term.getSubterm(1);
                trans_cong5=term.getSubterm(2);
                trans_cong6=term.getSubterm(3);
                trans_cong7=term.getSubterm(4);
                IStrategoList annos1=term.getAnnotations();
                trans_cong8=annos1;
                term=cong_arg_11.invoke(context,trans_cong3);
                if(term==null)break Fail53;
                trans_cong9=term;
                term=cong_arg_20.invoke(context,trans_cong4);
                if(term==null)break Fail53;
                trans_cong10=term;
                term=cong_arg_30.invoke(context,trans_cong5);
                if(term==null)break Fail53;
                trans_cong11=term;
                term=cong_arg_40.invoke(context,trans_cong6);
                if(term==null)break Fail53;
                trans_cong12=term;
                term=cong_arg_50.invoke(context,trans_cong7);
                if(term==null)break Fail53;
                term=termFactory.annotateTerm(termFactory.makeAppl(str2_example_xy_fused._consE_5,new IStrategoTerm[]{trans_cong9,trans_cong10,trans_cong11,trans_cong12,term}),checkListAnnos(termFactory,trans_cong8));
                context.popOnSuccess();
                if(true)return term;
            }
            context.popOnFailure();
            return null;
        }
    }
    @SuppressWarnings("all")public static class $Plus_2_0 extends Strategy{
        public static str2_example_xy_fused.$Plus_2_0 instance=new str2_example_xy_fused.$Plus_2_0();
        @Override public IStrategoTerm invoke(Context context,IStrategoTerm term,Strategy cong_arg_12,Strategy cong_arg_21){
            ITermFactory termFactory=context.getFactory();
            context.push("Plus_2_0");
            Fail54:{
                IStrategoTerm trans_cong16=null;
                IStrategoTerm trans_cong14=null;
                IStrategoTerm trans_cong15=null;
                IStrategoTerm trans_cong17=null;
                if(term.getTermType()!=IStrategoTerm.APPL||str2_example_xy_fused._consPlus_2!=((IStrategoAppl)term).getConstructor())break Fail54;
                trans_cong14=term.getSubterm(0);
                trans_cong15=term.getSubterm(1);
                IStrategoList annos2=term.getAnnotations();
                trans_cong16=annos2;
                term=cong_arg_12.invoke(context,trans_cong14);
                if(term==null)break Fail54;
                trans_cong17=term;
                term=cong_arg_21.invoke(context,trans_cong15);
                if(term==null)break Fail54;
                term=termFactory.annotateTerm(termFactory.makeAppl(str2_example_xy_fused._consPlus_2,new IStrategoTerm[]{trans_cong17,term}),checkListAnnos(termFactory,trans_cong16));
                context.popOnSuccess();
                if(true)return term;
            }
            context.popOnFailure();
            return null;
        }
    }
    @SuppressWarnings("all")public static class $Mult_2_0 extends Strategy{
        public static str2_example_xy_fused.$Mult_2_0 instance=new str2_example_xy_fused.$Mult_2_0();
        @Override public IStrategoTerm invoke(Context context,IStrategoTerm term,Strategy cong_arg_13,Strategy cong_arg_22){
            ITermFactory termFactory=context.getFactory();
            context.push("Mult_2_0");
            Fail55:{
                IStrategoTerm trans_cong21=null;
                IStrategoTerm trans_cong19=null;
                IStrategoTerm trans_cong20=null;
                IStrategoTerm trans_cong22=null;
                if(term.getTermType()!=IStrategoTerm.APPL||str2_example_xy_fused._consMult_2!=((IStrategoAppl)term).getConstructor())break Fail55;
                trans_cong19=term.getSubterm(0);
                trans_cong20=term.getSubterm(1);
                IStrategoList annos3=term.getAnnotations();
                trans_cong21=annos3;
                term=cong_arg_13.invoke(context,trans_cong19);
                if(term==null)break Fail55;
                trans_cong22=term;
                term=cong_arg_22.invoke(context,trans_cong20);
                if(term==null)break Fail55;
                term=termFactory.annotateTerm(termFactory.makeAppl(str2_example_xy_fused._consMult_2,new IStrategoTerm[]{trans_cong22,term}),checkListAnnos(termFactory,trans_cong21));
                context.popOnSuccess();
                if(true)return term;
            }
            context.popOnFailure();
            return null;
        }
    }
}


