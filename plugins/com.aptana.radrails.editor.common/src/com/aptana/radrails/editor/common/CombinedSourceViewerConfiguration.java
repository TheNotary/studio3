/**
 * This file Copyright (c) 2005-2009 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.radrails.editor.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * @author Max Stepanov
 *
 */
public abstract class CombinedSourceViewerConfiguration extends SourceViewerConfiguration {

	private ITokenScanner startEndTokenScanner;
	private ISourceViewerConfiguration defaultSourceViewerConfiguration;
	private ISourceViewerConfiguration primarySourceViewerConfiguration;
	
	/**
	 * @param defaultSourceViewerConfiguration
	 * @param primarySourceViewerConfiguration
	 */
	protected CombinedSourceViewerConfiguration(
			ISourceViewerConfiguration defaultSourceViewerConfiguration,
			ISourceViewerConfiguration primarySourceViewerConfiguration) {
		super();
		this.defaultSourceViewerConfiguration = defaultSourceViewerConfiguration;
		this.primarySourceViewerConfiguration = primarySourceViewerConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public final String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return TextUtils.combine(new String[][] {
				CombinedSwitchingPartitionScanner.SWITCHING_CONTENT_TYPES,
				defaultSourceViewerConfiguration.getContentTypes(),
				defaultSourceViewerConfiguration.getContentTypes()
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = (PresentationReconciler)  super.getPresentationReconciler(sourceViewer);
	
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getStartEndTokenScanner());
		reconciler.setDamager(dr, CombinedSwitchingPartitionScanner.START_SWITCH_TAG);
		reconciler.setRepairer(dr, CombinedSwitchingPartitionScanner.START_SWITCH_TAG);
		reconciler.setDamager(dr, CombinedSwitchingPartitionScanner.END_SWITCH_TAG);
		reconciler.setRepairer(dr, CombinedSwitchingPartitionScanner.END_SWITCH_TAG);
		
		defaultSourceViewerConfiguration.setupPresentationReconciler(reconciler, sourceViewer);
		primarySourceViewerConfiguration.setupPresentationReconciler(reconciler, sourceViewer);

		return reconciler;
	}
	
	protected abstract IPartitionerSwitchStrategy getPartitionerSwitchStrategy();
	
	private ITokenScanner getStartEndTokenScanner() {
		if (startEndTokenScanner == null) {
			RuleBasedScanner ts = new RuleBasedScanner();
			IToken seqToken = new Token(
					new TextAttribute(ColorManager.getDefault().getColor(ICommonColorConstants.START_END_SEQUENCE)));
			List<IRule> rules = new ArrayList<IRule>();
			for (String[] pair : getPartitionerSwitchStrategy().getSwitchTagPairs()) {
				rules.add(new SingleTagRule(pair[0], seqToken));
				rules.add(new SingleTagRule(pair[1], seqToken));
			}
			ts.setRules(rules.toArray(new IRule[rules.size()]));
			ts.setDefaultReturnToken(new Token(
					new TextAttribute(ColorManager.getDefault().getColor(ICommonColorConstants.DEFAULT))));
			startEndTokenScanner = ts;
		}
		return startEndTokenScanner;
	}

}
