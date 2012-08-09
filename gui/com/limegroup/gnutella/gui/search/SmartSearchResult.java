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
import java.io.File;

import javax.swing.JPopupMenu;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.search.db.TorrentDBPojo;
import com.limegroup.gnutella.gui.search.db.TorrentFileDBPojo;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SmartSearchResult extends AbstractSearchResult implements BittorrentSearchResult {

    private WebSearchResult _item;
    private SearchEngine _searchEngine;

    TorrentDBPojo torrent;
    TorrentFileDBPojo file;

    public SmartSearchResult(TorrentDBPojo torrentPojo, TorrentFileDBPojo torrentFilePojo) {
        torrent = torrentPojo;
        _item = new WebSearchResultProxy(torrent);
        file = torrentFilePojo;
        _searchEngine = SearchEngine.getSearchEngineById(torrentPojo.searchEngineID);
    }

    @Override
    public long getCreationTime() {
        return _item.getCreationTime();
    }

    @Override
    public String getExtension() {
        return file.relativePath.substring(file.relativePath.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public String getFileName() {
        if (file.relativePath.startsWith("/")) {
            file.relativePath = file.relativePath.substring(1);
        }

        return new File(file.relativePath).getName();
    }

    @Override
    public String getDisplayName() {

        if (file.relativePath.indexOf("/") != -1) {
            String fileName = file.relativePath.substring(file.relativePath.lastIndexOf("/"));

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        return file.relativePath.substring(0, file.relativePath.lastIndexOf("."));
    }

    @Override
    public int getQuality() {
        return 0;
    }

    public String getHash() {
        return _item.getHash();
    }

    public String getTorrentURI() {
        return _item.getTorrentURI();
    }

    @Override
    public long getSize() {
        return file.size;
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public String getVendor() {
        return _item.getVendor();
    }

    @Override
    public void initialize(SearchResultDataLine line) {
        line.setAddedOn(getCreationTime());

        //hack this to show the icon for mininova or for isohunt.
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openTorrentSearchResult(_item, file.relativePath);
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator resultPanel) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);

        PopupUtils.addMenuItem(SearchMediator.TORRENT_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    public int getSeeds() {
        return _item.getSeeds();
    }

    public SearchEngine getSearchEngine() {
        return _searchEngine;
    }

    public WebSearchResult getWebSearchResult() {
        return _item;
    }

    private class WebSearchResultProxy implements WebSearchResult {

        private TorrentDBPojo _torrentDBPojo;

        public WebSearchResultProxy(TorrentDBPojo torrentDBPojo) {
            _torrentDBPojo = torrentDBPojo;
        }

        @Override
        public String getFileName() {
            return _torrentDBPojo.fileName;
        }

        @Override
        public long getSize() {
            return _torrentDBPojo.size;
        }

        @Override
        public long getCreationTime() {
            return _torrentDBPojo.creationTime;
        }

        @Override
        public String getVendor() {
            return _torrentDBPojo.vendor;
        }

        @Override
        public String getHash() {
            return _torrentDBPojo.hash;
        }

        @Override
        public String getTorrentURI() {
            return _torrentDBPojo.torrentURI;
        }

        @Override
        public int getSeeds() {
            return _torrentDBPojo.seeds;
        }

        @Override
        public String getDetailsUrl() {
            return _torrentDBPojo.torrentDetailsURL;
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
