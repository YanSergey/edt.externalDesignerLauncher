# EDT External Designer Launcher

[![Download](https://img.shields.io/github/release/YanSergey/edt.externalDesignerLauncher?label=download&style=flat)](https://github.com/YanSergey/edt.externalDesignerLauncher/releases/latest)
[![GitHub Releases](https://img.shields.io/github/downloads/YanSergey/edt.externalDesignerLauncher/latest/total?style=flat-square)](https://github.com/YanSergey/edt.externalDesignerLauncher/releases)
[![GitHub All Releases](https://img.shields.io/github/downloads/YanSergey/edt.externalDesignerLauncher/total?style=flat-square)](https://github.com/YanSergey/edt.externalDesignerLauncher/releases)

Запускатель альтернативного Конфигуратора 1С для EDT

## 1. Установка:
### 1.1 Через файл
1. Скачать zip файл со страницы релизов [![Download](https://img.shields.io/github/release/YanSergey/edt.externalDesignerLauncher?label=download&style=flat)](https://github.com/YanSergey/edt.externalDesignerLauncher/releases/latest)
2. Зайти в главное меню EDT "Справка / Установить новое ПО"
3. Если репозиторий еще не добавлен нажать "Добавить", затем "Архив", выбрать скачанный файл.
4. Выбрать репозиторий, выбрать плагин, нажать "Далее" и пройти далее по шагам мастера установки
### 1.2 Через сайт обновлений
1. Зайти в главное меню EDT "Справка / Установить новое ПО"
2. Если репозиторий еще не добавлен нажать "Добавить", в поле "Расположение" вставить путь к сайту обновлений https://yansergey.github.io/edt.externalDesignerLauncher, в поле "Имя" вставить "EDT Eexternal Designer Launcher"
4. Выбрать репозиторий, выбрать плагин, нажать "Далее" и пройти далее по шагам мастера установки




## 2. Настройка

В конфигурационном файле "***1cedt.ini***" создать запись с путем к исполяемому файлу альтернативного конфигуратора.

`Пример:`

        -DalternativeDesignerLauncher=C:\snegopat_trial\core\starter.exe

## 3. Использование
В контекстное меню списка информационных баз добавлен пункт *"Запустить альтернативный Конфигуратор"*:

![Menu](/img/LaunchView.png "Меню с пунктом")

Перед запуском альтернативного конфигуратора автоматически завершается сессия агента конфигуратора, для освобождения информационной базы