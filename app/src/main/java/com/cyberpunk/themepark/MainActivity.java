package com.cyberpunk.themepark;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.pm.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE=2077, CREATE_EXPORT=2078;
    private LinearLayout root, list;
    private JSONObject state;
    private String currentId="";
    private boolean loadingProfiles=false;
    private ArrayList<ResolveInfo> apps=new ArrayList<>();
    private ArrayList<String> appPackages=new ArrayList<>();
    private ArrayList<String> appLabels=new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7,10,18));
        getWindow().setNavigationBarColor(Color.rgb(7,10,18));
        loadState();
        loadApps();
        buildScreen();
    }
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;
    }
    private void loadState(){
        try { state=new JSONObject(getPreferences(0).getString("profiles_state","{}")); }
        catch(Exception e){state=new JSONObject();}
        try {
            if(!state.has("profiles") || state.getJSONArray("profiles").length()==0){
                JSONArray a=new JSONArray(); JSONObject p=new JSONObject();
                currentId=UUID.randomUUID().toString();p.put("id",currentId);p.put("name","Primeiro salvamento");p.put("icons",new JSONObject());a.put(p);state.put("profiles",a);state.put("current",currentId);saveState();
            } else currentId=state.optString("current",state.getJSONArray("profiles").getJSONObject(0).optString("id"));
        } catch(Exception e){}
    }
    private void saveState(){getPreferences(0).edit().putString("profiles_state",state.toString()).apply();}
    private JSONArray profiles(){return state.optJSONArray("profiles");}
    private JSONObject profile(String id){
        JSONArray a=profiles();if(a!=null)for(int i=0;i<a.length();i++){JSONObject p=a.optJSONObject(i);if(p!=null&&id.equals(p.optString("id")))return p;}return null;
    }
    private JSONObject current(){JSONObject p=profile(currentId);if(p==null&&profiles()!=null&&profiles().length()>0){p=profiles().optJSONObject(0);currentId=p.optString("id");}return p;}
    private JSONObject icons(){JSONObject p=current();if(p==null)return new JSONObject();JSONObject x=p.optJSONObject("icons");if(x==null){x=new JSONObject();try{p.put("icons",x);}catch(Exception e){}}return x;}
    private File profileFolder(String id){File f=new File(getFilesDir(),"saves/"+id);if(!f.exists())f.mkdirs();return f;}
    private void loadApps(){
        Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found=getPackageManager().queryIntentActivities(i,0);
        Collections.sort(found,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(a.loadLabel(getPackageManager())),String.valueOf(b.loadLabel(getPackageManager()))));
        apps.clear();appPackages.clear();appLabels.clear();
        for(ResolveInfo r:found){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;apps.add(r);appPackages.add(pkg);appLabels.add(String.valueOf(r.loadLabel(getPackageManager())));}
    }
    private void buildScreen(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(14),dp(14),dp(14),dp(8));root.setBackgroundColor(Color.rgb(10,12,20));
        TextView title=text("CYBERPUNK 2077",25,Color.rgb(0,240,255));title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);root.addView(title);
        TextView sub=text("THEME PARK STUDIO  //  SAVED ICON SETS",10,Color.rgb(255,45,110));sub.setGravity(Gravity.CENTER);root.addView(sub);
        TextView saveLabel=text("SALVAMENTO ATIVO",11,Color.LTGRAY);saveLabel.setPadding(0,dp(14),0,dp(3));root.addView(saveLabel);
        Spinner spinner=new Spinner(this);ArrayList<String> names=new ArrayList<>();JSONArray ps=profiles();for(int n=0;ps!=null&&n<ps.length();n++)names.add(ps.optJSONObject(n).optString("name"));
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);spinner.setAdapter(adapter);
        int selected=0;for(int n=0;ps!=null&&n<ps.length();n++)if(ps.optJSONObject(n).optString("id").equals(currentId))selected=n;
        loadingProfiles=true;spinner.setSelection(selected);loadingProfiles=false;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){} public void onItemSelected(AdapterView<?> p,View v,int pos,long id){if(loadingProfiles)return;JSONObject x=profiles().optJSONObject(pos);if(x!=null){currentId=x.optString("id");stateFromCurrent();refreshList();}}});
        root.addView(spinner);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(0);
        addAction(actions,"NOVO",v->createProfile());
        addAction(actions,"RENOMEAR",v->renameProfile());
        addAction(actions,"DUPLICAR",v->duplicateProfile());
        addAction(actions,"EXCLUIR",v->deleteProfile());
        root.addView(actions);
        Button add=new Button(this);add.setText("+  IMPORTAR IMAGEM");add.setTextColor(Color.BLACK);add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0,240,255)));root.addView(add);
        add.setOnClickListener(v->pickImage());
        Button export=new Button(this);export.setText("EXPORTAR SALVAMENTO (.ZIP)");export.setTextColor(Color.WHITE);export.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(180,35,85)));root.addView(export);
        export.setOnClickListener(v->startExport());
        TextView hint=text("Selecione um aplicativo para associar um ícone. As alterações ficam neste salvamento; os outros permanecem separados.",11,Color.LTGRAY);hint.setPadding(0,dp(8),0,dp(8));root.addView(hint);
        ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);refreshList();
    }
    private void addAction(LinearLayout row,String label,View.OnClickListener click){Button b=new Button(this);b.setText(label);b.setTextSize(9);b.setPadding(dp(2),0,dp(2),0);b.setMinHeight(dp(42));row.addView(b,new LinearLayout.LayoutParams(0,dp(44),1));b.setOnClickListener(click);}
    private void stateFromCurrent(){statePutCurrent();saveState();}
    private void statePutCurrent(){try{state.put("current",currentId);}catch(Exception e){}}
    private void refreshList(){
        if(list==null)return;list.removeAllViews();JSONObject assigned=icons();
        TextView count=text("APLICATIVOS INSTALADOS: "+apps.size()+"   |   ÍCONES ASSOCIADOS: "+assigned.length(),10,Color.rgb(0,220,235));count.setPadding(0,dp(6),0,dp(8));list.addView(count);
        for(int n=0;n<apps.size();n++){
            final int index=n;ResolveInfo info=apps.get(n);String pkg=appPackages.get(n),label=appLabels.get(n),src=assigned.optString(pkg,"");
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(6),dp(5),dp(4),dp(5));row.setBackgroundColor(Color.rgb(20,25,38));
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(70));rp.bottomMargin=dp(4);list.addView(row,rp);
            ImageView appIcon=new ImageView(this);try{appIcon.setImageDrawable(info.loadIcon(getPackageManager()));}catch(Exception e){}row.addView(appIcon,new LinearLayout.LayoutParams(dp(42),dp(42)));
            LinearLayout words=new LinearLayout(this);words.setOrientation(1);words.setPadding(dp(8),0,dp(4),0);row.addView(words,new LinearLayout.LayoutParams(0,-2,1));
            words.addView(text(label,13,Color.WHITE));words.addView(text(src.isEmpty()?"Sem ícone associado":displaySource(src),9,src.isEmpty()?Color.LTGRAY:Color.rgb(0,230,210)));
            Button choose=new Button(this);choose.setText("Ícone");choose.setTextSize(10);choose.setMinHeight(dp(36));row.addView(choose);choose.setOnClickListener(v->chooseIcon(pkg,label));
            row.setOnClickListener(v->chooseIcon(pkg,label));
        }
    }
    private String displaySource(String s){return s.startsWith("res:")?s.substring(4):s;}
    private void chooseIcon(String pkg,String appName){
        ArrayList<String> sources=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        try{for(java.lang.reflect.Field f:R.drawable.class.getFields()){String n=f.getName();if(n.equals("ic_pack")||n.startsWith("abc_")||n.startsWith("notification_")||n.startsWith("design_"))continue;sources.add("res:"+n);labels.add(n);}}catch(Exception e){}
        File[] files=profileFolder(currentId).listFiles();if(files!=null)for(File f:files)if(f.isFile()&&f.getName().toLowerCase(Locale.ROOT).endsWith(".png")){sources.add("file:"+f.getName());labels.add(f.getName());}
        if(labels.isEmpty()){new AlertDialog.Builder(this).setMessage("Importe uma imagem primeiro.").setPositiveButton("OK",null).show();return;}
        String[] opts=labels.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("Ícone para "+appName).setItems(opts,(d,which)->{try{icons().put(pkg,sources.get(which));saveState();refreshList();}catch(Exception e){}}).setNegativeButton("Cancelar",null).setNeutralButton("Remover associação",(d,w)->{icons().remove(pkg);saveState();refreshList();}).show();
    }
    private void pickImage(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,PICK_IMAGE);}
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
        if(request==PICK_IMAGE){Uri uri=data.getData();try{String name="custom_"+System.currentTimeMillis()+".png";File dest=new File(profileFolder(currentId),name);try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(dest)){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}chooseAppForImage(name);}catch(Exception e){toast("Falha ao importar imagem.");}}
        else if(request==CREATE_EXPORT){try{writeExport(data.getData());toast("Salvamento exportado.");}catch(Exception e){toast("Falha ao exportar salvamento.");}}
    }
    private void chooseAppForImage(String file){
        String[] labels=appLabels.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Associar imagem a qual aplicativo?").setItems(labels,(d,w)->{try{icons().put(appPackages.get(w),"file:"+file);saveState();refreshList();toast("Ícone associado a "+appLabels.get(w));}catch(Exception e){}}).setNegativeButton("Depois", (d,w)->refreshList()).show();
    }
    private void createProfile(){EditText input=new EditText(this);input.setHint("Nome do salvamento");new AlertDialog.Builder(this).setTitle("Novo salvamento").setView(input).setPositiveButton("Criar",(d,w)->{String name=input.getText().toString().trim();if(name.isEmpty())name="Salvamento "+(profiles().length()+1);try{JSONObject p=new JSONObject();currentId=UUID.randomUUID().toString();p.put("id",currentId);p.put("name",name);p.put("icons",new JSONObject());profiles().put(p);statePutCurrent();saveState();buildScreen();}catch(Exception e){}}).setNegativeButton("Cancelar",null).show();}
    private void renameProfile(){EditText input=new EditText(this);input.setText(current().optString("name"));new AlertDialog.Builder(this).setTitle("Renomear salvamento").setView(input).setPositiveButton("Salvar",(d,w)->{String s=input.getText().toString().trim();if(!s.isEmpty())try{current().put("name",s);saveState();buildScreen();}catch(Exception e){}}).setNegativeButton("Cancelar",null).show();}
    private void duplicateProfile(){try{JSONObject old=current(),p=new JSONObject();String id=UUID.randomUUID().toString();p.put("id",id);p.put("name",old.optString("name")+" - cópia");JSONObject copied=new JSONObject(old.optJSONObject("icons").toString());p.put("icons",copied);File src=profileFolder(currentId),dst=profileFolder(id);File[] files=src.listFiles();if(files!=null)for(File f:files)if(f.isFile())copy(f,new File(dst,f.getName()));profiles().put(p);currentId=id;statePutCurrent();saveState();buildScreen();}catch(Exception e){toast("Não foi possível duplicar.");}}
    private void deleteProfile(){if(profiles().length()<=1){toast("Mantenha pelo menos um salvamento.");return;}new AlertDialog.Builder(this).setTitle("Excluir salvamento?").setMessage("As imagens e associações deste salvamento serão apagadas.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{File dir=profileFolder(currentId);deleteTree(dir);for(int i=0;i<profiles().length();i++)if(profiles().optJSONObject(i).optString("id").equals(currentId)){profiles().remove(i);break;}currentId=profiles().optJSONObject(0).optString("id");statePutCurrent();saveState();buildScreen();}).show();}
    private void startExport(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,current().optString("name","Cyberpunk")+"-icons.zip");startActivityForResult(i,CREATE_EXPORT);}
    private void writeExport(Uri uri)throws Exception{
        JSONObject map=icons();StringBuilder xml=new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        try(ZipOutputStream zip=new ZipOutputStream(getContentResolver().openOutputStream(uri))){
            zip.putNextEntry(new ZipEntry("appfilter.xml"));
            for(String pkg:map.keySet()){String src=map.optString(pkg);String drawable=src.startsWith("res:")?src.substring(4):src.startsWith("file:")?src.substring(5).replace(".png",""):"";ResolveInfo ri=null;int ix=appPackages.indexOf(pkg);if(ix>=0)ri=apps.get(ix);if(ri==null||drawable.isEmpty())continue;xml.append("  <item component=\"ComponentInfo{").append(pkg).append("/").append(ri.activityInfo.name).append("}\" drawable=\"").append(drawable).append("\" />\n");}
            xml.append("</resources>\n");zip.write(xml.toString().getBytes("UTF-8"));zip.closeEntry();
            zip.putNextEntry(new ZipEntry("README.txt"));zip.write(("Cyberpunk Theme Park - "+current().optString("name")+"\nExportação das associações e imagens. O arquivo appfilter.xml é destinado a launchers compatíveis; no Samsung Theme Park, use as imagens exportadas para aplicar os ícones manualmente.\n").getBytes("UTF-8"));zip.closeEntry();
            for(String pkg:map.keySet()){String src=map.optString(pkg);String name=src.startsWith("res:")?src.substring(4)+".png":src.startsWith("file:")?src.substring(5):"";if(name.isEmpty())continue;zip.putNextEntry(new ZipEntry("res/drawable-nodpi/"+name));if(src.startsWith("file:")){try(InputStream in=new FileInputStream(new File(profileFolder(currentId),src.substring(5)))){pipe(in,zip);}}else{int id=getResources().getIdentifier(src.substring(4),"drawable",getPackageName());Bitmap bm=BitmapFactory.decodeResource(getResources(),id);if(bm!=null){bm.compress(Bitmap.CompressFormat.PNG,100,zip);bm.recycle();}}zip.closeEntry();}
        }
    }
    private void pipe(InputStream in,OutputStream out)throws IOException{byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}
    private void copy(File a,File b)throws IOException{try(InputStream in=new FileInputStream(a);OutputStream out=new FileOutputStream(b)){pipe(in,out);}}
    private void deleteTree(File f){if(f.isDirectory()){File[] c=f.listFiles();if(c!=null)for(File x:c)deleteTree(x);}f.delete();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
