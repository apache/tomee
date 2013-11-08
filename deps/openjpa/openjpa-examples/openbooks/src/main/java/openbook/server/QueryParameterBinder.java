/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package openbook.server;

import javax.persistence.Query;

/**
 * Decorates a query by binding parameters.
 *  
 * @author Pinaki Poddar
 *
 */
public class QueryParameterBinder implements QueryDecorator {
    private final Object[] params;
    
    /**
     * Construct a parameter binder with the given parameters.
     * 
     * @param params
     */
    public QueryParameterBinder(Object...params) {
        this.params = params;
    }
    
    @Override
    public void decorate(Query query) {
        if (params == null)
            return;
        for (int i = 0; i < params.length; i += 2) {
            if (params[i] instanceof Integer) {
                query.setParameter((Integer)params[i], params[i+1]);
            } else if (params[i] instanceof String) {
                query.setParameter((String)params[i], params[i+1]);
            } else {
                throw new IllegalArgumentException("Parameter index " + params[i] + 
                        " is neither and integer nor String");
            }
        }
    }
}
