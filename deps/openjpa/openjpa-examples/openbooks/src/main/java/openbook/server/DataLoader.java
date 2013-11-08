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

import java.util.Map;

import openbook.util.PropertyHelper;

/**
 * Cleans and loads a new OpenBooks database.
 * 
 * @author Pinaki Poddar
 *
 */
public class DataLoader {
    
    /**
     * Load data.
     * 
     * @param args 0-th argument is the name of persistence unit
     *             1-st argument is the path to the data file
     *             
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String unit = args.length > 0 ? args[0] : OpenBookService.DEFAULT_UNIT_NAME;
        Map<String,Object> params = args.length > 1 ? PropertyHelper.load(args[1]) : null;
        OpenBookService service = ServiceFactory.getService(unit);
        service.clean();
        service.initialize(params);
    }
}
