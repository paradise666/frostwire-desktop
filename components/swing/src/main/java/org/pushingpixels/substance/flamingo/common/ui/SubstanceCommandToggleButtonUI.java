/*
 * Copyright (c) 2005-2010 Flamingo / Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.substance.flamingo.common.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.*;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
//import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandToggleButtonUI;
import org.pushingpixels.flamingo.internal.ui.common.ResizableIconUIResource;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;
import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.lafwidget.animation.effects.GhostPaintingUtils;
import org.pushingpixels.lafwidget.animation.effects.GhostingListener;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;
import org.pushingpixels.substance.api.shaper.SubstanceButtonShaper;
import org.pushingpixels.substance.flamingo.utils.*;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.ModelStateInfo;
import org.pushingpixels.substance.internal.utils.*;
import org.pushingpixels.substance.internal.utils.icon.TransitionAware;

/**
 * UI for command buttons {@link JCommandToggleButton} in <b>Substance </b> look
 * and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceCommandToggleButtonUI extends BasicCommandToggleButtonUI
		implements ActionPopupTransitionAwareUI {
	/**
	 * Delegate for painting the background.
	 */
	protected ButtonBackgroundDelegate backgroundDelegate;

	/**
	 * Property change listener. Listens on changes to
	 * {@link AbstractButton#MODEL_CHANGED_PROPERTY} property.
	 */
	protected PropertyChangeListener substancePropertyListener;

	/**
	 * Model change listener for ghost image effects.
	 */
	private GhostingListener substanceModelChangeListener;

	/**
	 * Tracker for visual state transitions.
	 */
	protected CommandButtonVisualStateTracker substanceVisualStateTracker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent comp) {
		SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
		return new SubstanceCommandToggleButtonUI();
	}

	/**
	 * Creates a new UI delegate for ribbon button.
	 */
	public SubstanceCommandToggleButtonUI() {
		super();
		this.backgroundDelegate = new ButtonBackgroundDelegate();

		this.substanceVisualStateTracker = new CommandButtonVisualStateTracker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasiccommandButtonUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		this.commandButton.putClientProperty(
				SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
				ClassicButtonShaper.INSTANCE);

		this.commandButton.setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.ui.BasicCommandButtonUI#updateBorder()
	 */
	@Override
	protected void updateBorder() {
		Border currBorder = this.commandButton.getBorder();
		if ((currBorder == null) || (currBorder instanceof UIResource)) {
			Insets extra = SubstanceSizeUtils
					.getDefaultBorderInsets(SubstanceSizeUtils
							.getComponentFontSize(this.commandButton));
			double hgapScaleFactor = this.commandButton.getHGapScaleFactor();
			double vgapScaleFactor = this.commandButton.getVGapScaleFactor();

			int top = 1 + (int) (vgapScaleFactor * extra.top);
			int left = 2 + (int) (hgapScaleFactor * (1 + extra.left));
			int bottom = 0 + (int) (vgapScaleFactor * extra.bottom);
			int right = 2 + (int) (hgapScaleFactor * (1 + extra.right));
			this.commandButton
					.setBorder(new BorderUIResource.EmptyBorderUIResource(top,
							left, bottom, right));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasiccommandButtonUI#installListeners()
	 */
	@Override
	protected void installListeners() {
		super.installListeners();

		this.substanceVisualStateTracker.installListeners(this.commandButton);

		this.substancePropertyListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (AbstractButton.MODEL_CHANGED_PROPERTY.equals(evt
						.getPropertyName())) {
					if (substanceModelChangeListener != null)
						substanceModelChangeListener.unregisterListeners();
					substanceModelChangeListener = new GhostingListener(
							commandButton, commandButton.getActionModel());
					substanceModelChangeListener.registerListeners();
				}
			}
		};
		this.commandButton
				.addPropertyChangeListener(this.substancePropertyListener);

		this.substanceModelChangeListener = new GhostingListener(
				this.commandButton, this.commandButton.getActionModel());
		this.substanceModelChangeListener.registerListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.ribbon.ui.BasiccommandButtonUI#uninstallListeners()
	 */
	@Override
	protected void uninstallListeners() {
		this.substanceVisualStateTracker.uninstallListeners(this.commandButton);
		this.substanceVisualStateTracker = null;

		this.commandButton
				.removePropertyChangeListener(this.substancePropertyListener);
		this.substancePropertyListener = null;

		this.substanceModelChangeListener.unregisterListeners();
		this.substanceModelChangeListener = null;

		super.uninstallListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#paintButtonIcon(java
	 * .awt.Graphics, java.awt.Rectangle)
	 */
	@Override
	protected void paintButtonIcon(Graphics g, Rectangle iconRect) {
		JCommandToggleButton jctb = (JCommandToggleButton) this.commandButton;
		Icon regular = jctb.getIcon();
		if (toUseDisabledIcon()
				&& (jctb.getDisabledIcon() != null)
				&& ((regular != null) && !regular.getClass()
						.isAnnotationPresent(TransitionAware.class))) {
			regular = jctb.getDisabledIcon();
		}

		boolean useThemed = SubstanceCoreUtilities
				.useThemedDefaultIcon(this.commandButton);

		if ((iconRect == null) || (regular == null) || (iconRect.width == 0)
				|| (iconRect.height == 0)) {
			return;
		}

		if (regular != null) {
			Graphics2D g2d = (Graphics2D) g.create();

			GhostPaintingUtils.paintGhostIcon(g2d, jctb, regular, iconRect);
			g2d.setComposite(LafWidgetUtilities.getAlphaComposite(jctb, g));

			if (!useThemed) {
				regular.paintIcon(jctb, g2d, iconRect.x, iconRect.y);
			} else {
				CommandButtonBackgroundDelegate.paintThemedCommandButtonIcon(
						g2d, iconRect, jctb, regular, jctb.getActionModel(),
						this.substanceVisualStateTracker
								.getPopupStateTransitionTracker());
			}
			g2d.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#isPaintingBackground()
	 */
	@Override
	protected boolean isPaintingBackground() {
		if (super.isPaintingBackground())
			return true;
		return (this.getActionTransitionTracker().getFacetStrength(
				ComponentStateFacet.ROLLOVER) > 0.0f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#isPaintingSeparators()
	 */
	@Override
	protected boolean isPaintingSeparators() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#paint(java.awt.Graphics
	 * , javax.swing.JComponent)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setFont(FlamingoUtilities.getFont(this.commandButton,
				"Ribbon.font", "Button.font", "Panel.font"));

		this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton,
				g);
		commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);
		commandButton.putClientProperty("icon", commandButton.getIcon());

		if (this.isPaintingBackground()) {
			this.paintButtonBackground(g2d, new Rectangle(0, 0, c.getWidth(), c
					.getHeight()));
		}

		// action model should be used to
		// compute the foreground color of the command button's text
		ModelStateInfo modelStateInfo = this.substanceVisualStateTracker
				.getActionStateTransitionTracker().getModelStateInfo();
		Color fgColor = this.getForegroundColor(modelStateInfo);

		if (layoutInfo.textLayoutInfoList != null) {
			for (CommandButtonLayoutManager.TextLayoutInfo mainTextLayoutInfo : layoutInfo.textLayoutInfoList) {
				if (mainTextLayoutInfo.text != null) {
					SubstanceTextUtilities.paintText(g2d, c,
							mainTextLayoutInfo.textRect,
							mainTextLayoutInfo.text, -1, g2d.getFont(),
							fgColor, g2d.getClipBounds());
				}
			}
		}

		if (layoutInfo.extraTextLayoutInfoList != null) {
			Color disabledFgColor = SubstanceColorSchemeUtilities
					.getColorScheme(this.commandButton,
							ComponentState.DISABLED_UNSELECTED)
					.getForegroundColor();
			float buttonAlpha = SubstanceColorSchemeUtilities.getAlpha(
					this.commandButton, ComponentState.DISABLED_UNSELECTED);
			if (buttonAlpha < 1.0f) {
				Color bgFillColor = SubstanceColorUtilities
						.getBackgroundFillColor(this.commandButton);
				disabledFgColor = SubstanceColorUtilities.getInterpolatedColor(
						disabledFgColor, bgFillColor, buttonAlpha);
			}
			if (modelStateInfo.getCurrModelState().isDisabled()) {
				disabledFgColor = SubstanceColorUtilities.getInterpolatedColor(
						disabledFgColor, SubstanceColorUtilities
								.getBackgroundFillColor(c), 0.5);
			}
			for (CommandButtonLayoutManager.TextLayoutInfo extraTextLayoutInfo : layoutInfo.extraTextLayoutInfoList) {
				if (extraTextLayoutInfo.text != null) {
					SubstanceTextUtilities.paintText(g2d, c,
							extraTextLayoutInfo.textRect,
							extraTextLayoutInfo.text, -1, g2d.getFont(),
							disabledFgColor, g2d.getClipBounds());
				}
			}
		}

		if (layoutInfo.iconRect != null) {
			this.paintButtonIcon(g2d, layoutInfo.iconRect);
		}

		g2d.dispose();
	}

	protected Color getForegroundColor(ModelStateInfo modelStateInfo) {
		Color fgColor = this.commandButton.getForeground();
		if (fgColor instanceof UIResource) {
			float buttonAlpha = SubstanceColorSchemeUtilities.getAlpha(
					this.commandButton, modelStateInfo.getCurrModelState());
			fgColor = SubstanceTextUtilities.getForegroundColor(
					this.commandButton, this.commandButton.getText(),
					modelStateInfo, buttonAlpha);
		}
		return fgColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#getPreferredSize(javax
	 * .swing.JComponent)
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {
		AbstractCommandButton button = (AbstractCommandButton) c;
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(button);

		Dimension superPref = super.getPreferredSize(button);
		if (superPref == null)
			return null;

		if (shaper == null)
			return superPref;

		// fix for issue 35 on Flamingo - do not enforce
		// min size on buttons in the ribbon
		if ((button.getDisplayState() == CommandButtonDisplayState.MEDIUM)
				/*&& (SwingUtilities.getAncestorOfClass(AbstractRibbonBand.class,
						button) == null)*/) {
			JButton dummy = new JButton(button.getText(), button.getIcon());
			return shaper.getPreferredSize(dummy, superPref);
		}
		return superPref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#paintButtonBackground
	 * (java.awt.Graphics, java.awt.Rectangle)
	 */
	@Override
	protected void paintButtonBackground(Graphics graphics, Rectangle toFill) {
		if (SubstanceCoreUtilities.isButtonNeverPainted(this.commandButton))
			return;

		ButtonModel actionModel = this.commandButton.getActionModel();

		SubstanceFillPainter fillPainter = SubstanceCoreUtilities
				.getFillPainter(this.commandButton);
		SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
				.getBorderPainter(this.commandButton);

		boolean ignoreSelections = this.commandButton instanceof JCommandToggleMenuButton;
		BufferedImage fullAlphaBackground = CommandButtonBackgroundDelegate
				.getFullAlphaBackground(this.commandButton, actionModel,
						fillPainter, borderPainter, this.commandButton
								.getWidth(), this.commandButton.getHeight(),
						this.getActionTransitionTracker(), ignoreSelections);

		StateTransitionTracker.ModelStateInfo modelStateInfo = getActionTransitionTracker()
				.getModelStateInfo();
		Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = ignoreSelections ? modelStateInfo
				.getStateNoSelectionContributionMap()
				: modelStateInfo.getStateContributionMap();

		// Two special cases here:
		// 1. Button has flat appearance.
		// 2. Button is disabled.
		// For both cases, we need to set custom translucency.
		boolean isFlat = this.commandButton.isFlat();
		boolean isSpecial = isFlat || !this.commandButton.isEnabled();
		float extraAlpha = 1.0f;
		if (isSpecial) {
			if (isFlat) {
				extraAlpha = 0.0f;
				for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
						.entrySet()) {
					ComponentState activeState = activeEntry.getKey();
					if (activeState.isDisabled())
						continue;
					if (activeState == ComponentState.ENABLED)
						continue;
					extraAlpha += activeEntry.getValue().getContribution();
				}
			} else {
				ComponentState actionAreaState = ComponentState.getState(
						actionModel, this.commandButton);
				if (actionAreaState.isDisabled()) {
					extraAlpha = SubstanceColorSchemeUtilities.getAlpha(
							this.commandButton, actionAreaState);
				}
			}
		}
		// System.out.println(extraAlpha);
		extraAlpha = Math.min(1.0f, extraAlpha);
		if (extraAlpha > 0.0f) {
			Graphics2D g2d = (Graphics2D) graphics.create();
			g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
					this.commandButton, extraAlpha, graphics));
			g2d.drawImage(fullAlphaBackground, 0, 0, null);
			g2d.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.ui.BasicCommandButtonUI#syncDisabledIcon()
	 */
	@Override
	protected void syncDisabledIcon() {
		ResizableIcon currDisabledIcon = this.commandButton.getDisabledIcon();
		ResizableIcon icon = this.commandButton.getIcon();
		if ((currDisabledIcon == null)
				|| ((currDisabledIcon instanceof UIResource) && !currDisabledIcon
						.getClass().isAnnotationPresent(TransitionAware.class))) {
			if (icon != null) {
				this.commandButton.setDisabledIcon(new ResizableIconUIResource(
						new SubstanceDisabledResizableIcon(icon)));
			} else {
				this.commandButton.setDisabledIcon(null);
			}
		} else {
			// disabled icon coming from app code
			if (icon != null) {
				this.commandButton.getDisabledIcon()
						.setDimension(
								new Dimension(icon.getIconWidth(), icon
										.getIconHeight()));
			}
		}
	}

	@Override
	public StateTransitionTracker getActionTransitionTracker() {
		return this.substanceVisualStateTracker
				.getActionStateTransitionTracker();
	}

	@Override
	public StateTransitionTracker getPopupTransitionTracker() {
		return this.substanceVisualStateTracker
				.getPopupStateTransitionTracker();
	}

	@Override
	public StateTransitionTracker getTransitionTracker() {
		return this.substanceVisualStateTracker
				.getActionStateTransitionTracker();
	}

	@Override
	public boolean isInside(MouseEvent me) {
		return this.getLayoutInfo().actionClickArea.contains(me.getPoint());
	}
}
