/***
 Copyright (c) 2012 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.richedit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.HashMap;

public class EditorActionModeCallback {
    protected int menuResource = 0;
    protected RichEditText editor = null;
    protected Selection selection = null;
    protected EditorActionModeListener listener = null;
    protected HashMap<Integer, EditorActionModeCallback> chains =
            new HashMap<Integer, EditorActionModeCallback>();

    EditorActionModeCallback(int menuResource, RichEditText editor,
                             EditorActionModeListener listener) {
        this.menuResource = menuResource;
        this.editor = editor;
        this.listener = listener;
    }

    void setSelection(Selection selection) {
        this.selection = selection;
    }

    void addChain(int menuItemId, EditorActionModeCallback toChainTo) {
        chains.put(menuItemId, toChainTo);
    }

    @TargetApi(11)
    public static class Native extends EditorActionModeCallback implements
            ActionMode.Callback {
        Activity host = null;

        public Native(Activity host, int menuResource, RichEditText editor,
                      EditorActionModeListener listener) {
            super(menuResource, editor, listener);
            this.host = host;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();

            inflater.inflate(menuResource, menu);
            listener.setIsShowing(true);

            return (true);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (selection != null) {
                selection.apply(editor);
            }

            return (false);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listener.setIsShowing(false);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            EditorActionModeCallback next = chains.get(item.getItemId());

            if (next != null) {
                next.setSelection(new Selection(editor));
                host.startActionMode((EditorActionModeCallback.Native) next);
                mode.finish();

                return (true);
            }

            mode.finish();

            return (listener.doAction(item.getItemId()));
        }
    }
}
