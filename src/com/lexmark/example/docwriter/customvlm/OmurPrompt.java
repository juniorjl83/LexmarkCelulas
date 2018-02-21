package com.lexmark.example.docwriter.customvlm;


import com.lexmark.core.Element;
import com.lexmark.example.docwriter.Activator;
import com.lexmark.prtapp.profile.VlmlNavigator;
import com.lexmark.prtapp.prompt.DisplayType;
import com.lexmark.prtapp.prompt.PromptEventResult;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.VlmlPrompt;
import com.lexmark.prtapp.prompt.VlmlPromptContext;

public class OmurPrompt implements VlmlPrompt
{
   private String pregunta;
   private String idPregunta;
   private String[] opciones;
   private boolean exit = false;
   private String respuesta;
   private int seleccion;
   
   /** We need to keep track of the VLML prompt context ourselves here */
   VlmlPromptContext context = null;
   /** Name of the top level VLML layout */
   String vlmlName = "HardPrompt";
   /** Keep track of how we are dismissed so the profile can know */
   String dismissButton = "";

   public OmurPrompt(String idPregunta, String pregunta, String[] opciones, int seleccion)
   {
      this.idPregunta = idPregunta;
      this.pregunta = pregunta;
      this.opciones = opciones;
      this.seleccion = seleccion;
      this.respuesta = String.valueOf(seleccion);
   }

   public String getVlml()
   {
      StringBuffer screen = new StringBuffer();
      screen.append("<?xml version=\"1.0\"?>\n");
      screen.append("<GridLayout distribution=\"heterogeneous\" spacing=\"0\" columns=\"1\" rows=\"4\" name=\"omur\">\n");
      screen.append(" <AttachChild bottom=\"1\" top=\"0\" right=\"1\" left=\"0\" yPadding=\"4\" xPadding=\"4\" yGFill=\"shrink\" xGFill=\"expandFill\">\n");
      screen.append("     <Label name=\"title\" justification=\"center\" text=\"" + pregunta + "\" style=\"bold\" size=\"medium\"/>\n");
      screen.append(" </AttachChild>\n");
      screen.append(" <AttachChild bottom=\"2\" top=\"1\" right=\"1\" left=\"0\" yPadding=\"0\" xPadding=\"0\" yGFill=\"expand\" xGFill=\"expandShrink\">\n");
      screen.append("     <RadioGroup name=\"opciones\" deselectedImage=\"commonListButtonUp\" selectedImage=\"commonListButtonDown\" orientation=\"vertical\" selectedItem=\"" + seleccion + "\">\n");      
      
      for (int i = 0; i < opciones.length; i++){
         String opcion = opciones[i];
         screen.append("            <RadioButton name=\"" + i + "\" text=\"" + opcion + "\"/>\n");
      }
      screen.append("     </RadioGroup>\n");
      screen.append(" </AttachChild>\n");
      
      screen.append("   </AttachChild>\n");
      screen.append(" <AttachChild bottom=\"3\" top=\"2\" right=\"1\" left=\"0\" yPadding=\"0\" xPadding=\"0\" yGFill=\"shrink\" xGFill=\"shrink\">\n");
      screen.append("     <Image name=\"separatorBar\" imageName=\"NavRowBottomLine\"/>\n");
      screen.append(" </AttachChild>\n");
      
      screen.append(" <AttachChild bottom=\"4\" top=\"3\" right=\"1\" left=\"0\" yPadding=\"0\" xPadding=\"0\" yGFill=\"shrink\" xGFill=\"expandFill\">\n");
      screen.append("     <BoxLayout name=\"navBar\" color=\"b2b2b2\" orientation=\"horizontal\">\n");

      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"next\" overlayStyle=\"bold\" overlayPointSize=\"16\">\n");
      screen.append("                 <Normal text=\"Siguiente\" imageName=\"navRowOptionsUp\"/>\n");
      screen.append("                 <Selected text=\"Siguiente\" imageName=\"navRowOptionsDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
      
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"back\" overlayStyle=\"bold\" overlayPointSize=\"16\">\n");
      screen.append("                 <Normal text=\"Atrás\" imageName=\"navRowOptionsUp\"/>\n");
      screen.append("                 <Selected text=\"Atrás\" imageName=\"navRowOptionsDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
            
      screen.append("         <AttachChildToTheEnd fill=\"shrink\">\n");
      screen.append("             <LabeledImageButton name=\"cancel\">\n");
      screen.append("                 <Normal imageName=\"homeUp\"/>\n");
      screen.append("                 <Selected imageName=\"homeDown\"/>\n");
      screen.append("             </LabeledImageButton>\n");
      screen.append("         </AttachChildToTheEnd>\n");
      
      screen.append("     </BoxLayout>");
      screen.append(" </AttachChild>\n");
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
      return "Omur-pregunta-" + idPregunta;
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
                             
      if ( component.equals("omur.navBar.next") ){
         Activator.getLog().info("entra a next");
         dismissButton = "next";
         if ( respuesta != null ){
            result = PromptEventResult.VALIDATE;   
         }else{
            result = PromptEventResult.CONTINUE;
         }
      }else if ( component.equals("omur.navBar.back") ){
         Activator.getLog().info("entra a back");
         dismissButton = "back";
         result = PromptEventResult.VALIDATE;
      }else if (component.equals("omur.navBar.cancel")){
         Activator.getLog().info("entra a cancel");
         dismissButton = "cancel";
         result = PromptEventResult.VALIDATE;
      }else if ( component.equals("omur.opciones") ){
         Activator.getLog().info("entra a " + component);
         dismissButton = component;
         respuesta = data.stringValue();
         result = PromptEventResult.CONTINUE;
      }else{
         Activator.getLog().info("entra a else");
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
