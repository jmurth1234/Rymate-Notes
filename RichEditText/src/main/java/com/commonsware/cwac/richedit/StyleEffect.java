/***
  Copyright (c) 2008-2011 CommonsWare, LLC
  
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

import android.text.Spannable;
import android.text.style.StyleSpan;

public class StyleEffect extends Effect<Boolean> {
  private int style;

  StyleEffect(int style) {
    this.style=style;
  }

  @Override
  boolean existsInSelection(RichEditText editor) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();
    boolean result=false;

    if (selection.start != selection.end) {
      for (StyleSpan span : getStyleSpans(str, selection)) {
        if (span.getStyle() == style) {
          result=true;
          break;
        }
      }
    }
    else {
      StyleSpan[] spansBefore=
          str.getSpans(selection.start - 1, selection.end,
                       StyleSpan.class);
      StyleSpan[] spansAfter=
          str.getSpans(selection.start, selection.end + 1,
                       StyleSpan.class);

      for (StyleSpan span : spansBefore) {
        if (span.getStyle() == style) {
          result=true;
          break;
        }
      }

      if (result) {
        result=false;

        for (StyleSpan span : spansAfter) {
          if (span.getStyle() == style) {
            result=true;
            break;
          }
        }
      }
    }

    return(result);
  }

  @Override
  Boolean valueInSelection(RichEditText editor) {
    return(existsInSelection(editor));
  }

  @Override
  void applyToSelection(RichEditText editor, Boolean add) {
    applyToSpannable(editor.getText(), new Selection(editor), add);
  }

  void applyToSpannable(Spannable str, Selection selection, Boolean add) {
    int prologueStart=Integer.MAX_VALUE;
    int epilogueEnd=-1;

    for (StyleSpan span : getStyleSpans(str, selection)) {
      if (span.getStyle() == style) {
        int spanStart=str.getSpanStart(span);

        if (spanStart < selection.start) {
          prologueStart=Math.min(prologueStart, spanStart);
        }

        int spanEnd=str.getSpanEnd(span);

        if (spanEnd > selection.end) {
          epilogueEnd=Math.max(epilogueEnd, spanEnd);
        }

        str.removeSpan(span);
      }
    }

    if (add) {
      str.setSpan(new StyleSpan(style), selection.start, selection.end,
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    else {
      if (prologueStart < Integer.MAX_VALUE) {
        str.setSpan(new StyleSpan(style), prologueStart,
                    selection.start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }

      if (epilogueEnd > -1) {
        str.setSpan(new StyleSpan(style), selection.end, epilogueEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
  }

  private StyleSpan[] getStyleSpans(Spannable str, Selection selection) {
    return(str.getSpans(selection.start, selection.end, StyleSpan.class));
  }
}
