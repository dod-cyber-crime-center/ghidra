/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package docking.options.editor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditorSupport;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import docking.DialogComponentProvider;
import docking.DockingWindowManager;
import docking.widgets.label.GDHtmlLabel;

/**
 * Color editor that is a bit unusual in that its custom component is a button that when pushed,
 * pops up a dialog for editing the color. Use {@link ColorPropertyEditor} for a more traditional
 * property editor that returns a direct color editing component.
 */
public class ColorEditor extends PropertyEditorSupport {

	private static final String LIGHT_COLOR = "SILVER";
	private static final String DARK_COLOR = "BLACK";

	private static GhidraColorChooser colorChooser;

	private JLabel previewLabel = new GDHtmlLabel();
	private Color color;
	private Color lastUserSelectedColor;

	public ColorEditor() {
		previewLabel.setOpaque(true);
		previewLabel.setPreferredSize(new Dimension(100, 20));
		previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		previewLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				// show the editor to get the user value
				showDialog(evt.getComponent());

				ColorEditor.this.firePropertyChange();

				// now set the new value
				updateColor(color);
			}
		});
	}

	private void showDialog(Component parentComponent) {
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new ColorEditorPanel(), BorderLayout.CENTER);
		EditorProvider provider = new EditorProvider(dialogPanel);
		DockingWindowManager.showDialog(previewLabel, provider);
	}

	@Override
	public Component getCustomEditor() {
		return previewLabel;
	}

	@Override
	public boolean supportsCustomEditor() {
		return true;
	}

	@Override
	public void setValue(Object value) {
		color = (Color) value;
		lastUserSelectedColor = color;
		updateColor(color);
	}

	private void updateColor(Color newColor) {
		String colorString = LIGHT_COLOR;

		// change the color to a darker value if the color being set is light
		int colorValue = newColor.getRed() + newColor.getGreen() + newColor.getBlue();
		if (colorValue > 400) {  // arbitrary threshold determined by trial-and-error
			colorString = DARK_COLOR;
		}

		previewLabel.setText(
			"<HTML><CENTER><I><FONT SIZE=2 COLOR=" + colorString + ">click</FONT></I></CENTER>");

		previewLabel.setBackground(color);
	}

	@Override
	public Object getValue() {
		return color;
	}

	@Override
	public boolean isPaintable() {
		return false;
	}

	@Override
	public void paintValue(Graphics gfx, Rectangle box) {
		if (color != null) {
			gfx.setColor(color);
		}
		else {
			gfx.setColor(Color.black);
		}
		gfx.fillRect(box.x, box.y, box.width, box.height);
	}

	/////////////////////////////////////////////////////////////////////////

	class EditorProvider extends DialogComponentProvider {
		EditorProvider(JPanel contentPanel) {
			super("Color Editor", true);

			addWorkPanel(contentPanel);
			addOKButton();
			addCancelButton();
		}

		@Override
		protected void okCallback() {
			color = lastUserSelectedColor;
			close();
		}
	}

	class ColorEditorPanel extends JPanel {

		ColorEditorPanel() {

			setLayout(new BorderLayout());

			if (colorChooser == null) {
				colorChooser = new GhidraColorChooser();
			}

			add(colorChooser, BorderLayout.CENTER);
			colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					lastUserSelectedColor = colorChooser.getColor();
					// This could be a ColorUIResource, but Options only support storing Color.
					lastUserSelectedColor =
						new Color(lastUserSelectedColor.getRed(), lastUserSelectedColor.getGreen(),
							lastUserSelectedColor.getBlue(), lastUserSelectedColor.getAlpha());
				}
			});
			colorChooser.setColor(color);
		}
	}
}
