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

package com.frostwire.content;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.core.providers.FilesProvider;
import com.frostwire.database.Cursor;
import com.frostwire.net.Uri;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ContentResolver {

    private static List<ContentProvider> providers;

    private final Context context;

    public ContentResolver(Context context) {
        this.context = context;
    }

    /**
     * <p>
     * Query the given URI, returning a {@link Cursor} over the result set.
     * </p>
     * <p>
     * For best performance, the caller should follow these guidelines:
     * <ul>
     * <li>Provide an explicit projection, to prevent
     * reading data from storage that aren't going to be used.</li>
     * <li>Use question mark parameter markers such as 'phone=?' instead of
     * explicit values in the {@code selection} parameter, so that queries
     * that differ only by those values will be recognized as the same
     * for caching purposes.</li>
     * </ul>
     * </p>
     *
     * @param uri The URI, using the content:// scheme, for the content to
     *         retrieve.
     * @param projection A list of which columns to return. Passing null will
     *         return all columns, which is inefficient.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
     *         clause (excluding the ORDER BY itself). Passing null will use the
     *         default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry, or null
     * @see Cursor
     */
    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            return null;
        }
        try {
            Cursor qCursor = provider.query(uri, projection, selection, selectionArgs, sortOrder);
            if (qCursor == null) {
                releaseProvider(provider);
                return null;
            }
            // force query execution
            qCursor.getCount();

            return qCursor;
        } catch (RuntimeException e) {
            releaseProvider(provider);
            throw e;
        }
    }

    /**
     * Inserts a row into a table at the given URL.
     *
     * If the content provider supports transactions the insertion will be atomic.
     *
     * @param uri The URL of the table to insert into.
     * @param values The initial values for the newly inserted row. The key is the column name for
     *               the field. Passing an empty ContentValues will create an empty row.
     * @return the URL of the newly created row.
     */
    public final Uri insert(Uri uri, ContentValues values) {
        ContentProvider provider = acquireProvider(uri);
        if (provider == null) {
            return null;
        }
        try {
            Uri createdRow = provider.insert(uri, values);
            return createdRow;
        } catch (RuntimeException e) {
            // Arbitrary and not worth documenting, as Activity
            // Manager will kill this process shortly anyway.
            return null;
        } finally {
            releaseProvider(provider);
        }
    }

    private ContentProvider acquireProvider(Uri uri) {
        setupProviders();

        ContentProvider provider = null;

        for (ContentProvider p : providers) {
            try {
                String mime = p.getType(uri);
                if (mime != null) {
                    provider = p;
                    break;
                }
            } catch (Throwable e) {
                // ignore
            }
        }

        return provider;
    }

    private void releaseProvider(ContentProvider provider) {
    }

    private synchronized void setupProviders() {
        if (providers == null) {
            providers = new ArrayList<ContentProvider>();

            ContentProvider provider = null;

            provider = new FilesProvider(context);
            provider.onCreate();
            providers.add(provider);
        }
    }
}
