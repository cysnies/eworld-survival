package uk.org.whoami.authme.gui.screens;

import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.gui.Clickable;
import uk.org.whoami.authme.gui.CustomButton;
import uk.org.whoami.authme.settings.SpoutCfg;

public class LoginScreen extends GenericPopup implements Clickable {
   public AuthMe plugin = AuthMe.getInstance();
   private SpoutCfg spoutCfg = SpoutCfg.getInstance();
   private CustomButton exitBtn;
   private CustomButton loginBtn;
   private GenericTextField passBox;
   private GenericLabel titleLbl;
   private GenericLabel textLbl;
   private GenericLabel errorLbl;
   String exitTxt;
   String loginTxt;
   String exitMsg;
   String title;
   List textlines;
   public SpoutPlayer splayer;

   public LoginScreen(SpoutPlayer player) {
      super();
      this.exitTxt = this.spoutCfg.getString("LoginScreen.exit button");
      this.loginTxt = this.spoutCfg.getString("LoginScreen.login button");
      this.exitMsg = this.spoutCfg.getString("LoginScreen.exit message");
      this.title = this.spoutCfg.getString("LoginScreen.title");
      this.textlines = this.spoutCfg.getList("LoginScreen.text");
      this.splayer = player;
      this.createScreen();
   }

   private void createScreen() {
      int objects = this.textlines.size() + 4;
      int part = this.textlines.size() > 5 ? 195 / objects : 20;
      int h = 3 * part / 4;
      int w = 8 * part;
      this.titleLbl = new GenericLabel();
      this.titleLbl.setText(this.title).setTextColor(new Color(1.0F, 0.0F, 0.0F, 1.0F)).setAlign(WidgetAnchor.TOP_CENTER).setHeight(h).setWidth(w).setX(this.maxWidth / 2).setY(25);
      this.attachWidget(this.plugin, this.titleLbl);
      int ystart = 25 + h + part / 2;

      for(int x = 0; x < this.textlines.size(); ++x) {
         this.textLbl = new GenericLabel();
         this.textLbl.setText((String)this.textlines.get(x)).setAlign(WidgetAnchor.TOP_CENTER).setHeight(h).setWidth(w).setX(this.maxWidth / 2).setY(ystart + x * part);
         this.attachWidget(this.plugin, this.textLbl);
      }

      this.passBox = new GenericTextField();
      this.passBox.setMaximumCharacters(18).setMaximumLines(1).setHeight(h - 2).setWidth(w - 2).setY(220 - h - 2 * part);
      this.passBox.setPasswordField(true);
      this.setXToMid(this.passBox);
      this.attachWidget(this.plugin, this.passBox);
      this.errorLbl = new GenericLabel();
      this.errorLbl.setText("").setTextColor(new Color(1.0F, 0.0F, 0.0F, 1.0F)).setHeight(h).setWidth(w).setX(this.passBox.getX() + this.passBox.getWidth() + 2).setY(this.passBox.getY());
      this.attachWidget(this.plugin, this.errorLbl);
      this.loginBtn = new CustomButton(this);
      this.loginBtn.setText(this.loginTxt).setHeight(h).setWidth(w).setY(220 - h - part);
      this.setXToMid(this.loginBtn);
      this.attachWidget(this.plugin, this.loginBtn);
      this.exitBtn = new CustomButton(this);
      this.exitBtn.setText(this.exitTxt).setHeight(h).setWidth(w).setY(220 - h);
      this.setXToMid(this.exitBtn);
      this.attachWidget(this.plugin, this.exitBtn);
      this.setPriority(RenderPriority.Highest);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void handleClick(ButtonClickEvent event) {
      Button b = event.getButton();
      SpoutPlayer player = event.getPlayer();
      if (!event.isCancelled() && event != null && event.getPlayer() != null) {
         if (b.equals(this.loginBtn)) {
            this.plugin.management.performLogin(player, this.passBox.getText(), false);
         } else if (b.equals(this.exitBtn)) {
            event.getPlayer().kickPlayer(this.exitMsg);
         }

      }
   }

   private void setXToMid(Widget w) {
      w.setX((this.maxWidth - w.getWidth()) / 2);
   }
}
