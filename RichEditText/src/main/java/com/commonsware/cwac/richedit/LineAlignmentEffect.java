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

import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;

public class LineAlignmentEffect extends Effect<Layout.Alignment> {
  @Override
  boolean existsInSelection(RichEditText editor) {
    return(valueInSelection(editor)!=null);
  }

  @Override
  Layout.Alignment valueInSelection(RichEditText editor) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();
    AlignmentSpan.Standard[] spans=getAlignmentSpans(str, selection);

    if (spans.length>0) {
      return(spans[0].getAlignment());
    }
    
    return(null);
  }

  @Override
  void applyToSelection(RichEditText editor, Layout.Alignment alignment) {
    Selection selection=new Selection(editor);
    Spannable str=editor.getText();

    for (AlignmentSpan.Standard span : getAlignmentSpans(str, selection)) {
      str.removeSpan(span);
    }

    if (alignment!=null) {
      str.setSpan(new AlignmentSpan.Standard(alignment), selection.start, selection.end,
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private AlignmentSpan.Standard[] getAlignmentSpans(Spannable str,
                                                     Selection selection) {
    return(str.getSpans(selection.start, selection.end,
                        AlignmentSpan.Standard.class));
  }
}
