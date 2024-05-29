# lab 1
Реализовать приложение дополненной реальности на основе примера:

Android: https://developers.google.com/ar/develop/augmented-faces
https://www.youtube.com/watch?v=-4EvaCQpVEQ&list=PLOU2XLYxmsILHvpAkROp2dXz-jQi4S4_y

Запускать приложения нужно на устройстве.

# lab 2
Настроить работу с одним из шлемов по инструкциям:
HTC VIVE (https://www.vive.com/au/support/vive/category_howto/about-the-headset.html)
https://www.wikihow.com/Set-Up-Your-HTC-Vive
HP Reverb (https://support.hp.com/us-en/document/c06433655)
Собрать и запустить проект https://github.com/Unity-Technologies/XR-Interaction-Toolkit-Examples

Добавить простое взаимодействие с любым объектом с помощью контроллера:

изменения материала
звук
анимация
применение физических сил

# lab 3

Реализовать распознавание плоскостей и расположение объектов на плоскости через AR Anchor.

Добавить одну или несколько дополнительных возможностей: 
Удаление указанного объекта
Манипуляции с указанным объектом (поворот, масштаб, расположение)
Взаимодействие объектов:
Запрет пересечения объектов при расстановке
Если два объекта ставятся рядом они соединяются
Движение по заданной траектории или за курсором-камерой
Изменение внешнего вида объекта при взаимодействии с ним.
Примеры
Android: https://www.kodeco.com/6986535-arcore-with-kotlin-getting-started
iOS: https://developer.apple.com/documentation/arkit/arkit_in_ios/environmental_analysis/placing_objects_and_handling_3d_interaction
Unity: https://codelabs.developers.google.com/arcore-unity-ar-foundation?hl=ru

# lab 4

Реализовать распознавание меток с помощью AR:
Выбрать произвольную картинку (с учетом ограничений, накладываемых на метки). 
Добавить компоненты для работы с Image Tracker.
Добавить на сцену 3D модель (например).
Привязать появление модели к распознаванию картинки.
Добавить взаимодействие с объектом по прикосновению к экрану.
Примеры:

iOS: https://developer.apple.com/documentation/arkit/content_anchors/detecting_images_in_an_ar_experience
Android: https://codelabs.developers.google.com/codelabs/augimg-intro
https://github.com/google-ar/sceneform-android-sdk/tree/v1.15.0/samples/augmentedimage
Unity: https://github.com/Unity-Technologies/arfoundation-samples/tree/main/Assets/Scenes/ImageTracking

# lab 5 

Реализовать расстановку меток с привязкой к географическим координатам:
Реализовать отображение объектов с привязкой к географическим координатам.
Отображать из локальной базы несколько последних добавленных меток (Anchor) .
Включить отсечение по расстоянию до метки.
Добавить возможность добавлять метки по текущему положению пользователя.
Примеры:
ARCore: https://developers.google.com/ar/develop/java/geospatial/codelab#0
ARKit: https://developer.apple.com/documentation/arkit/arkit_in_ios/content_anchors/tracking_geographic_locations_in_ar
Unity: https://docs.unity-ar-gps-location.com/guide/
