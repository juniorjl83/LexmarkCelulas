package com.lexmark.example.docwriter.customvlm;


import com.lexmark.core.Element;
import com.lexmark.example.docwriter.Activator;
import com.lexmark.prtapp.profile.VlmlNavigator;
import com.lexmark.prtapp.prompt.DisplayType;
import com.lexmark.prtapp.prompt.PromptEventResult;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.VlmlPrompt;
import com.lexmark.prtapp.prompt.VlmlPromptContext;

public class EditBoxPrompt implements VlmlPrompt
{
   private String pregunta;
   private String idPregunta;
   private String texto;
   private boolean exit = false;
   private String respuesta;
   private String prefijo;
   
   /** We need to keep track of the VLML prompt context ourselves here */
   VlmlPromptContext context = null;
   /** Name of the top level VLML layout */
   String vlmlName = "HardPrompt";
   /** Keep track of how we are dismissed so the profile can know */
   String dismissButton = "";

   public EditBoxPrompt(String idPregunta, String pregunta, String texto, String prefijo)
   {
      this.idPregunta = idPregunta;
      this.pregunta = pregunta;
      this.texto = texto;
      this.respuesta = texto;
      this.prefijo = prefijo.length() > 0 ? prefijo + "_" : "";
   }

   public String getVlml()
   {
      StringBuffer screen = new StringBuffer();
      screen.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      screen.append("<!-- VLML COOKBOOK - WVGA - EDITBOX EXAMPLE  -->");
      screen.append("<GridLayout name=\"text\" rows=\"6\" columns=\"2\" verticalScroll=\"never\" horizontalScroll=\"never\" distribution=\"heterogeneous\">");
      screen.append("   <!-- TITLE LABEL  -->");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"shrink\" xPadding=\"4\" yPadding=\"4\" left=\"0\" right=\"2\" top=\"0\" bottom=\"1\">");
      screen.append("      <Label name=\"label\" size=\"medium\" style=\"bold\" text=\"" + pregunta + "\" justification=\"center\" maxHeight=\"15\" />");
      screen.append("   </AttachChild>");
      screen.append("   <!-- EDITBOX  -->");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"shrink\" xPadding=\"0\" yPadding=\"0\" left=\"0\" right=\"1\" top=\"1\" bottom=\"2\">");
      screen.append("      <Label name=\"label\" size=\"medium\" style=\"normal\" text=\""+ prefijo +"\" justification=\"right\" maxHeight=\"15\" />");
      screen.append("   </AttachChild>");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"shrink\" xPadding=\"20\" yPadding=\"0\" left=\"1\" right=\"2\" top=\"1\" bottom=\"2\">");
      screen.append("      <EditBox name=\"entryField\" text=\""+ texto +"\" maxLength=\"70\" visible=\"true\" focused=\"true\" />");
      screen.append("   </AttachChild>");
      screen.append("   <!-- MIN MAX LABEL  -->");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"fill\" xPadding=\"4\" yPadding=\"0\" left=\"0\" right=\"2\" top=\"2\" bottom=\"3\">");
      screen.append("      <Label name=\"minmaxlabel\" text=\"Caracteres minimo 3. Caracteres Maximo 70\" justification=\"center\" size=\"small\" />");
      screen.append("   </AttachChild>");
      screen.append("   <!-- KEYBOARD  -->");
      screen.append("   <AttachChild xGFill=\"shrink\" yGFill=\"expandFill\" xPadding=\"0\" yPadding=\"2\" left=\"0\" right=\"2\" top=\"3\" bottom=\"4\">");
      screen.append("      <Keyboard name=\"keypad\" type=\"alphanumeric\" />");
      screen.append("   </AttachChild>");
      screen.append("   <!-- HORIZONTAL SEPARATION LINE  -->");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"fill\" xPadding=\"0\" yPadding=\"0\" left=\"0\" right=\"2\" top=\"4\" bottom=\"5\">");
      screen.append("      <Image name=\"separatorLine\" imageName=\"NavRowBottomLine\" />");
      screen.append("   </AttachChild>");
      screen.append("   <!-- NAVIGATION BAR -->");
      screen.append("   <AttachChild xGFill=\"expandFill\" yGFill=\"fill\" xPadding=\"0\" yPadding=\"0\" left=\"0\" right=\"2\" top=\"5\" bottom=\"6\">");
      screen.append("      <BoxLayout name=\"navBar\" orientation=\"horizontal\" color=\"b2b2b2\">");
      
      screen.append("         <!-- NEXT BUTTON  -->");
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">");
      screen.append("            <LabeledImageButton name=\"next\" overlayPointSize=\"16\" overlayStyle=\"bold\">");
      screen.append("               <Normal imageName=\"navRowOptionsUp\" text=\"Siguiente\" />");
      screen.append("               <Selected imageName=\"navRowOptionsDown\" text=\"Siguiente\" />");
      screen.append("            </LabeledImageButton>");
      screen.append("         </AttachChildToTheEnd>");
      
      screen.append("         <!-- BACK BUTTON  -->");
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">");
      screen.append("            <LabeledImageButton name=\"back\" overlayPointSize=\"16\" overlayStyle=\"bold\">");
      screen.append("               <Normal imageName=\"optionsMiddleUp\" text=\"Atrás\" />");
      screen.append("               <Selected imageName=\"optionsMiddleDown\" text=\"Atrás\" />");
      screen.append("            </LabeledImageButton>");
      screen.append("         </AttachChildToTheEnd>");
      
      screen.append("         <!-- CANCEL BUTTON  -->");
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">");
      screen.append("            <LabeledImageButton name=\"cancel\">");
      screen.append("               <Normal imageName=\"homeUp\" />");
      screen.append("               <Selected imageName=\"homeDown\" />");
      screen.append("            </LabeledImageButton>");
      screen.append("         </AttachChildToTheEnd>");
      screen.append("      </BoxLayout>");
      screen.append("   </AttachChild>");
      screen.append("</GridLayout>");

      
      return screen.toString();
   }

   public String getDismissButton()
   {
      return dismissButton;
   }

   public DisplayType getDisplayType()
   {
      return DisplayType.VLML;
   }

   public String getHelp()
   {
      return null;
   }

   public String getId()
   {
      return "text-pregunta-" + idPregunta;
   }

   public String getLabel()
   {
      return null;
   }

   public String getName()
   {
      return vlmlName;
   }

   public void setHelp(String arg0)
   {

   }

   public void setLabel(String arg0)
   {

   }

   public void setName(String name)
   {
      this.vlmlName = name;
   }

   public void dismissed()
   {

   }

   public PromptEventResult handleEvent(String component, String event, Element data) throws PromptException
   {
      PromptEventResult result = null;
      Activator.getLog().info("EasyPrompt: got a VLML event!\n\tComponent = " + component + "\n\tEvent = " + event + "\n\t" + data.print("Data"));
                             
      if ( component.equals("text.navBar.next") ){
         Activator.getLog().info("entra a next");
         dismissButton = "next";
         if ( respuesta != null ){
            result = PromptEventResult.VALIDATE;   
         }else{
            result = PromptEventResult.CONTINUE;
         }
      }else if ( component.equals("text.navBar.back") ){
         Activator.getLog().info("entra a back");
         dismissButton = "back";
         result = PromptEventResult.VALIDATE;
      }else if (component.equals("text.navBar.cancel")){
         Activator.getLog().info("entra a cancel");
         dismissButton = "cancel";
         result = PromptEventResult.VALIDATE;
      }else{
         Activator.getLog().info("entra a else");
         respuesta = data.stringValue();
         result = PromptEventResult.CONTINUE;
      }
      
      return result;
   }

   public void init(VlmlPromptContext promptContext, VlmlNavigator navigator)
   {
      this.context = promptContext;

   }

   public boolean validate() throws PromptException
   {
      Activator.getLog().info("entra a validate");
      return true;
   }

   public boolean getExit(){
      return exit;
   }

   public String getRespuesta()
   {
      return respuesta;
   }

   public void setRespuesta(String value)
   {
      this.respuesta = value;
   }
}
