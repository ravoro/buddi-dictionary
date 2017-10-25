-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Хост: localhost
-- Время создания: Окт 25 2017 г., 12:34
-- Версия сервера: 5.7.20-0ubuntu0.16.04.1
-- Версия PHP: 7.0.22-0ubuntu0.16.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `dictdb`
--

-- --------------------------------------------------------

--
-- Структура таблицы `definitions`
--

CREATE TABLE `definitions` (
  `wid` int(11) NOT NULL,
  `definition` varchar(255) NOT NULL,
  `lang` varchar(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `definitions`
--

INSERT INTO `definitions` (`wid`, `definition`, `lang`) VALUES
(1, 'hz', 'en'),
(1, 'moldy cheese', 'en'),
(1, 'сыр', 'ru'),
(3, 'opopo', 'ru'),
(4, 'popopo', 'ru');

-- --------------------------------------------------------

--
-- Структура таблицы `languages`
--

CREATE TABLE `languages` (
  `code` varchar(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `languages`
--

INSERT INTO `languages` (`code`) VALUES
('en'),
('ru');

-- --------------------------------------------------------

--
-- Структура таблицы `play_evolutions`
--

CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` mediumtext,
  `revert_script` mediumtext,
  `state` varchar(255) DEFAULT NULL,
  `last_problem` mediumtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `play_evolutions`
--

INSERT INTO `play_evolutions` (`id`, `hash`, `applied_at`, `apply_script`, `revert_script`, `state`, `last_problem`) VALUES
(1, '3f3887511deea9dacf48318d1e00f00948e5e8e0', '2016-11-30 11:52:04', 'CREATE TABLE languages (\ncode VARCHAR(2) NOT NULL PRIMARY KEY\n);\nINSERT INTO languages VALUES ("en");\nINSERT INTO languages VALUES ("ru");\n\nCREATE TABLE words (\nid   INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,\nword VARCHAR(255) NOT NULL,\nlang VARCHAR(2)   NOT NULL,\nFOREIGN KEY (lang) REFERENCES languages (code)\n);\n\nCREATE TABLE definitions (\nwid        INTEGER      NOT NULL,\ndefinition VARCHAR(255) NOT NULL,\nlang       VARCHAR(2)   NOT NULL,\nFOREIGN KEY (wid) REFERENCES words (id),\nFOREIGN KEY (lang) REFERENCES languages (code)\n);', 'DROP TABLE languages;\nDROP TABLE words;\nDROP TABLE definitions;', 'applied', '');

-- --------------------------------------------------------

--
-- Структура таблицы `words`
--

CREATE TABLE `words` (
  `id` int(11) NOT NULL,
  `word` varchar(255) NOT NULL,
  `lang` varchar(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `words`
--

INSERT INTO `words` (`id`, `word`, `lang`) VALUES
(1, 'cheese', 'en'),
(2, 'horseradish', 'ru'),
(3, 'cheese', 'ru'),
(4, 'coop', 'en');

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `definitions`
--
ALTER TABLE `definitions`
  ADD PRIMARY KEY (`wid`,`definition`,`lang`),
  ADD KEY `lang` (`lang`);

--
-- Индексы таблицы `languages`
--
ALTER TABLE `languages`
  ADD PRIMARY KEY (`code`);

--
-- Индексы таблицы `play_evolutions`
--
ALTER TABLE `play_evolutions`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `words`
--
ALTER TABLE `words`
  ADD PRIMARY KEY (`id`),
  ADD KEY `lang` (`lang`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `words`
--
ALTER TABLE `words`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `definitions`
--
ALTER TABLE `definitions`
  ADD CONSTRAINT `definitions_ibfk_1` FOREIGN KEY (`wid`) REFERENCES `words` (`id`),
  ADD CONSTRAINT `definitions_ibfk_2` FOREIGN KEY (`lang`) REFERENCES `languages` (`code`);

--
-- Ограничения внешнего ключа таблицы `words`
--
ALTER TABLE `words`
  ADD CONSTRAINT `words_ibfk_1` FOREIGN KEY (`lang`) REFERENCES `languages` (`code`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
