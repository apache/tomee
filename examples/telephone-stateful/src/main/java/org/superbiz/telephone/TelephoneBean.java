/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//START SNIPPET: code
package org.superbiz.telephone;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.util.ArrayList;
import java.util.List;

@Remote
@Stateful
public class TelephoneBean implements Telephone {

    private static final String[] answers = {
            "How nice.",
            "Oh, of course.",
            "Interesting.",
            "Really?",
            "No.",
            "Definitely.",
            "I wondered about that.",
            "Good idea.",
            "You don't say!",
    };

    private final List<String> conversation = new ArrayList<String>();

    @Override
    public void speak(final String words) {
        conversation.add(words);
    }

    @Override
    public String listen() {
        if (conversation.size() == 0) {
            return "Nothing has been said";
        }

        final String lastThingSaid = conversation.get(conversation.size() - 1);
        return answers[Math.abs(lastThingSaid.hashCode()) % answers.length];
    }
}
//END SNIPPET: code