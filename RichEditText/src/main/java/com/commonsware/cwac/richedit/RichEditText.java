/***
 Copyright (c) 2011-2012 CommonsWare, LLC

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
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.style.StrikethroughSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom widget that simplifies adding rich text editing
 * capabilities to Android activities. Serves as a drop-in
 * replacement for EditText. Full documentation can be found
 * on project Web site
 * (http://github.com/commonsguy/cwac-richedit). Concepts in
 * this editor were inspired by:
 * http://code.google.com/p/droid-writer
 */
public class RichEditText extends EditText implements
        EditorActionModeListener {
    public static final Effect<Boolean> BOLD =
            new StyleEffect(Typeface.BOLD);
    public static final Effect<Boolean> ITALIC =
            new StyleEffect(Typeface.ITALIC);
    public static final Effect<Boolean> UNDERLINE = new UnderlineEffect();
    public static final Effect<Boolean> STRIKETHROUGH =
            new StrikethroughEffect();
    public static final Effect<Layout.Alignment> LINE_ALIGNMENT =
            new LineAlignmentEffect();
    public static final Effect<String> TYPEFACE = new TypefaceEffect();
    public static final Effect<Boolean> SUPERSCRIPT =
            new SuperscriptEffect();
    public static final Effect<Boolean> SUBSCRIPT = new SubscriptEffect();

    private static final ArrayList<Effect<?>> EFFECTS =
            new ArrayList<Effect<?>>();
    private boolean isSelectionChanging = false;
    private OnSelectionChangedListener selectionListener = null;
    private boolean actionModeIsShowing = false;
    private EditorActionModeCallback.Native mainMode = null;
    private boolean forceActionMode = false;
    private boolean keyboardShortcuts = true;

    /*
     * EFFECTS is a roster of all defined effects, for simpler
     * iteration over all the possibilities.
     */
    static {
    /*
     * Boolean effects
     */
        EFFECTS.add(BOLD);
        EFFECTS.add(ITALIC);
        EFFECTS.add(UNDERLINE);
        EFFECTS.add(STRIKETHROUGH);
        EFFECTS.add(SUPERSCRIPT);
        EFFECTS.add(SUBSCRIPT);

    /*
     * Non-Boolean effects
     */
        EFFECTS.add(LINE_ALIGNMENT);
        EFFECTS.add(TYPEFACE);
    }

    /*
     * Standard one-parameter widget constructor, simply
     * chaining to superclass.
     */
    public RichEditText(Context context) {
        super(context);
    }

    /*
     * Standard two-parameter widget constructor, simply
     * chaining to superclass.
     */
    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * Standard three-parameter widget constructor, simply
     * chaining to superclass.
     */
    public RichEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
     * If there is a registered OnSelectionChangedListener,
     * checks to see if there are any effects applied to the
     * current selection, and supplies that information to the
     * registrant.
     *
     * Uses isSelectionChanging to avoid updating anything
     * while this callback is in progress (e.g., registrant
     * updates a ToggleButton, causing its
     * OnCheckedChangeListener to fire, causing it to try to
     * update the RichEditText as if the user had clicked upon
     * it.
     *
     * @see android.widget.TextView#onSelectionChanged(int,
     * int)
     */
    @Override
    public void onSelectionChanged(int start, int end) {
        super.onSelectionChanged(start, end);

        if (selectionListener != null) {
            ArrayList<Effect<?>> effects = new ArrayList<Effect<?>>();

            for (Effect<?> effect : EFFECTS) {
                if (effect.existsInSelection(this)) {
                    effects.add(effect);
                }
            }

            isSelectionChanging = true;
            selectionListener.onSelectionChanged(start, end, effects);
            isSelectionChanging = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (forceActionMode && mainMode != null && start != end) {
                postDelayed(new Runnable() {
                    public void run() {
                        if (!actionModeIsShowing) {
                            startActionMode(mainMode);
                        }
                    }
                }, 500);
            }
        }

    }

    @TargetApi(11)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyboardShortcuts
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (event.isCtrlPressed()) {
                if (keyCode == KeyEvent.KEYCODE_B) {
                    toggleEffect(RichEditText.BOLD);

                    return (true);
                } else if (keyCode == KeyEvent.KEYCODE_I) {
                    toggleEffect(RichEditText.ITALIC);

                    return (true);
                } else if (keyCode == KeyEvent.KEYCODE_U) {
                    toggleEffect(RichEditText.UNDERLINE);

                    return (true);
                }
            }
        }

        return (super.onKeyUp(keyCode, event));
    }

    /*
     * Call this to provide a listener object to be notified
     * when the selection changes and what the applied effects
     * are for the current selection. Designed to be used by a
     * hosting activity to adjust states of toolbar widgets
     * (e.g., check/uncheck a ToggleButton).
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    /*
     * Call this to enable or disable handling of keyboard
     * shortcuts (e.g., Ctrl-B for bold). Enabled by default.
     */
    public void setKeyboardShortcutsEnabled(boolean keyboardShortcuts) {
        this.keyboardShortcuts = keyboardShortcuts;
    }

    /*
     * Call this to have an effect applied to the current
     * selection. You get the Effect object via the static
     * data members (e.g., RichEditText.BOLD). The value for
     * most effects is a Boolean, indicating whether to add or
     * remove the effect.
     */
    public <T> void applyEffect(Effect<T> effect, T value) {
        if (!isSelectionChanging) {
            effect.applyToSelection(this, value);
        }
    }

    /*
     * Returns true if a given effect is applied somewhere in
     * the current selection. This includes the effect being
     * applied in a subset of the current selection.
     */
    public boolean hasEffect(Effect<?> effect) {
        return (effect.existsInSelection(this));
    }

    /*
     * Returns the value of the effect applied to the current
     * selection. For Effect<Boolean> (e.g.,
     * RichEditText.BOLD), returns the same value as
     * hasEffect(). Otherwise, returns the highest possible
     * value, if multiple occurrences of this effect are
     * applied to the current selection. Returns null if there
     * is no such effect applied.
     */
    public <T> T getEffectValue(Effect<T> effect) {
        return (effect.valueInSelection(this));
    }

    /*
     * If the effect is presently applied to the current
     * selection, removes it; if the effect is not presently
     * applied to the current selection, adds it.
     */
    public void toggleEffect(Effect<Boolean> effect) {
        if (!isSelectionChanging) {
            effect.applyToSelection(this, !effect.valueInSelection(this));
        }
    }

    @Override
    public boolean doAction(int itemId) {
        if (itemId == R.id.cwac_richedittext_underline) {
            toggleEffect(RichEditText.UNDERLINE);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_strike) {
            toggleEffect(RichEditText.STRIKETHROUGH);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_superscript) {
            toggleEffect(RichEditText.SUPERSCRIPT);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_subscript) {
            toggleEffect(RichEditText.SUBSCRIPT);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_serif) {
            applyEffect(RichEditText.TYPEFACE, "serif");

            return (true);
        } else if (itemId == R.id.cwac_richedittext_sans) {
            applyEffect(RichEditText.TYPEFACE, "sans");

            return (true);
        } else if (itemId == R.id.cwac_richedittext_mono) {
            applyEffect(RichEditText.TYPEFACE, "monospace");

            return (true);
        } else if (itemId == R.id.cwac_richedittext_normal) {
            applyEffect(RichEditText.LINE_ALIGNMENT,
                    Layout.Alignment.ALIGN_NORMAL);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_center) {
            applyEffect(RichEditText.LINE_ALIGNMENT,
                    Layout.Alignment.ALIGN_CENTER);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_opposite) {
            applyEffect(RichEditText.LINE_ALIGNMENT,
                    Layout.Alignment.ALIGN_OPPOSITE);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_bold) {
            toggleEffect(RichEditText.BOLD);

            return (true);
        } else if (itemId == R.id.cwac_richedittext_italic) {
            toggleEffect(RichEditText.ITALIC);

            return (true);
        } else if (itemId == android.R.id.selectAll
                || itemId == android.R.id.cut || itemId == android.R.id.copy
                || itemId == android.R.id.paste) {
            onTextContextMenuItem(itemId);
        }

        return (false);
    }

    @Override
    public void setIsShowing(boolean isShowing) {
        actionModeIsShowing = isShowing;
    }

    public void enableActionModes(boolean forceActionMode) {
        this.forceActionMode = forceActionMode;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            enableNativeActionModes();
        }
    }

    @TargetApi(11)
    public void disableActionModes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setCustomSelectionActionModeCallback(null);
            mainMode = null;
        }
    }

    @TargetApi(11)
    private void enableNativeActionModes() {
        EditorActionModeCallback.Native effectsMode =
                new EditorActionModeCallback.Native(
                        (Activity) getContext(),
                        R.menu.cwac_richedittext_effects,
                        this, this);

        EditorActionModeCallback.Native fontsMode =
                new EditorActionModeCallback.Native(
                        (Activity) getContext(),
                        R.menu.cwac_richedittext_fonts,
                        this, this);

        mainMode =
                new EditorActionModeCallback.Native(
                        (Activity) getContext(),
                        R.menu.cwac_richedittext_main,
                        this, this);

        mainMode.addChain(R.id.cwac_richedittext_effects, effectsMode);
        mainMode.addChain(R.id.cwac_richedittext_fonts, fontsMode);

        EditorActionModeCallback.Native entryMode =
                new EditorActionModeCallback.Native(
                        (Activity) getContext(),
                        R.menu.cwac_richedittext_entry,
                        this, this);

        entryMode.addChain(R.id.cwac_richedittext_format, mainMode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setCustomSelectionActionModeCallback(entryMode);
        }
    }

    /*
     * Interface for listener object to be registered by
     * setOnSelectionChangedListener().
     */
    public interface OnSelectionChangedListener {
        /*
         * Provides details of the new selection, including the
         * start and ending character positions, and a roster of
         * all effects presently applied (so you can bulk-update
         * a toolbar when the selection changes).
         */
        void onSelectionChanged(int start, int end, List<Effect<?>> effects);
    }

    private static class UnderlineEffect extends
            SimpleBooleanEffect<UnderlineSpan> {
        UnderlineEffect() {
            super(UnderlineSpan.class);
        }
    }

    private static class StrikethroughEffect extends
            SimpleBooleanEffect<StrikethroughSpan> {
        StrikethroughEffect() {
            super(StrikethroughSpan.class);
        }
    }

    private static class SuperscriptEffect extends
            SimpleBooleanEffect<SuperscriptSpan> {
        SuperscriptEffect() {
            super(SuperscriptSpan.class);
        }
    }

    private static class SubscriptEffect extends
            SimpleBooleanEffect<SubscriptSpan> {
        SubscriptEffect() {
            super(SubscriptSpan.class);
        }
    }
}
