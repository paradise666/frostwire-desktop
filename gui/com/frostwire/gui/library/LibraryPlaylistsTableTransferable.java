/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
package com.frostwire.gui.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.frostwire.alexandria.PlaylistItem;
import com.limegroup.gnutella.gui.dnd.FileTransferable;

public class LibraryPlaylistsTableTransferable implements Transferable {

    public static final DataFlavor ITEM_ARRAY = new DataFlavor(LibraryPlaylistsTableTransferable.Item[].class, "LibraryPlaylistTransferable.Item Array");

    private final List<LibraryPlaylistsTableTransferable.Item> items;
    
    private final FileTransferable fileTransferable;

    public LibraryPlaylistsTableTransferable(List<PlaylistItem> playlistItems) {
        items = LibraryUtils.convertToItems(playlistItems);
        
        List<File> files = new ArrayList<File>(items.size());
        for (PlaylistItem item : playlistItems) {
            files.add(new File(item.getFilePath()));
        }
        fileTransferable = new FileTransferable(files);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        List<DataFlavor> list = new ArrayList<DataFlavor>();
        list.addAll(Arrays.asList(fileTransferable.getTransferDataFlavors()));
        list.add(ITEM_ARRAY);
        return list.toArray(new DataFlavor[0]);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ITEM_ARRAY) || fileTransferable.isDataFlavorSupported(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(ITEM_ARRAY) ) {
            return items.toArray(new Item[0]);
        } else {
            return fileTransferable.getTransferData(flavor);
        }
    }
    
    public static final class Item implements Serializable {
        private static final long serialVersionUID = 928701185904989565L;

        public Item() {
        }

        public int id;
        public String filePath;
        public String fileName;
        public long fileSize;
        public String fileExtension;
        public String trackTitle;
        public float trackDurationInSecs;
        public String trackArtist;
        public String trackAlbum;
        public String coverArtPath;
        public String trackBitrate;
        public String trackComment;
        public String trackGenre;
        public String trackNumber;
        public String trackYear;
        public boolean starred;
    }
}
