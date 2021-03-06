/*
* Copyright 2020 Aleksandr Dorogush
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.adorogush.mirotask.repository;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import java.util.List;
import java.util.Optional;

/** Instances of this interface provide repository features for Widgets. */
public interface WidgetRepository {

  Widget createOne(WidgetToCreate widgetToCreate);

  Optional<Widget> readOne(String id);

  List<Widget> readAll(int perPage, Integer fromZ);

  Optional<Widget> updateOne(String id, WidgetToUpdate widgetToUpdate);

  Optional<Widget> deleteOne(String id);
}
