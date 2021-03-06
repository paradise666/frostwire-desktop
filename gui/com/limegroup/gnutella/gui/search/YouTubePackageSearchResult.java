/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.websearch.youtube.YouTubeSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class YouTubePackageSearchResult extends AbstractSearchResult {

    private final YouTubeSearchResult sr;
    private final SearchEngine searchEngine;

    public YouTubePackageSearchResult(YouTubeSearchResult sr, SearchEngine searchEngine, String query) {
        super(query);
        this.sr = sr;
        this.searchEngine = searchEngine;
    }

    @Override
    public String getFileName() {
        return sr.getFileName();
    }

    @Override
    public String getDisplayName() {
        return sr.getDisplayName();
    }

    @Override
    public long getSize() {
        return sr.getSize();
    }

    @Override
    public long getCreationTime() {
        return sr.getCreationTime();
    }

    @Override
    public String getSource() {
        return sr.getSource();
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public void download(boolean partial) {
        GUIMediator.instance().openYouTubeVideoUrl(sr.getTorrentURI());
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download(false);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.YOUTUBE_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    @Override
    public String getHash() {
        return sr.getHash();
    }

    @Override
    public String getTorrentURI() {
        return sr.getTorrentURI();
    }

    @Override
    public int getSeeds() {
        return sr.getSeeds();
    }

    @Override
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    @Override
    public WebSearchResult getWebSearchResult() {
        return sr;
    }

    @Override
    public boolean allowDeepSearch() {
        return true;
    }
}
