package club.dagomys.lemmatisator.scr;

import io.github.kju2.languagedetector.LanguageDetector;
import io.github.kju2.languagedetector.language.Language;
import org.apache.lucene.morphology.Heuristic;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.Morphology;
import org.apache.lucene.morphology.MorphologyImpl;
import org.apache.lucene.morphology.analyzer.MorphologyAnalyzer;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LimmatisatorMain {
    public static void main(String[] args) {
        try {
            LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
            String russianText = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";
            String englishText = "I am sure learning foreign languages is very important nowadays. People start learning a foreign language, because they want to have a better job, a possibility to study abroad or take part in international conferences. People want to have a possibility to get a higher education abroad or even start their career there. The most popular among foreign languages are English, German, Italian, French and Spanish.";
            String french = "Ma ville\n" +
                    "J'habite une belle ville dans le nord de la France. Il y a un quartier très agréable pour aller se balader, lorsqu'il fait beau. Il est possible de faire du vélo dans un parc autour d'une citadelle fortifiée.";
            String test = "О библиотеке О библиотеке Правила библиотеки Пространство 3D-тур Документы Команда О нас в СМИ Новости библиотеки О Михаиле Светлове Наши друзья Коллегам Коллегам Проекты и мероприятия Методические материалы Партнерам Партнерам Наши друзья Пространство О нас в СМИ Брендбук и лого Личный кабинет Афиша Лекции Концерты Мастер-классы Детям Архив событий Проекты Тест - Какой вы Светлов? Текущие Для детей и родителей Портфолио Каталог Абонемент Молодежный медиацентр Детско-юношеский отдел Что почитать Новинки Рецензии Подборки Истории Персональные рекомендации Книжные лабиринты Что посмотреть Услуги Что вы можете получить бесплатно Что вы можете получить платно Афиша Лекции Концерты Мастер-классы Детям Архив событий Проекты Тест - Какой вы Светлов? Текущие Для детей и родителей Портфолио Каталог Абонемент Молодежный медиацентр Детско-юношеский отдел Что почитать Новинки Рецензии Подборки Истории Персональные рекомендации Книжные лабиринты Что посмотреть Услуги Что вы можете получить бесплатно Что вы можете получить платно О библиотеке Правила библиотеки Пространство 3D-тур Документы Команда О нас в СМИ Новости библиотеки О Михаиле Светлове Наши друзья Главная Что почитать Подборки Мир медиа, морали и мемориальной культуры Мир медиа, морали и мемориальной культуры 16.07.2020 Сентябрьским вечером в Светловку заглянула Оксана Мороз, доцент Шанинки, креативный директор Фонда Егора Гайдара, автор «Блога злобного культуролога». Вместе с гостями библиотеки она поговорила о современных сериалах от Игры Престолов до Чернобыля и выяснила, почему некоторые из них нас привлекают, а некоторые нет. Мы попросили Оксану порекомендовать нашим читателям список книг и получили интереснейшую подборку! 1. «Язык новых медиа» – книга, написанная Львом Мановичем на рубеже XX-XXI веков, но не утратившая оригинальности и точности в анализе современных медийных практик и предлагающая современные методы анализа онлайн источников и инструментов. Подойдёт тем, кто интересуется историей становления цифровых сервисов и – отдельно – сетевого искусства. 2. «Интернет животных: Новый диалог между человеком и природой» – захватывающая история дигитализации живого, рассказываемая Александром Пшера. В книге ставится вопрос о том, как будет выглядеть мир, где все живое будет включено в процесс производства и обмена данных. И одновременно дебатируется вопрос, насколько мир, управляемый людьми и их изобретениями, в котором оцифрованы будет все и вся, сможет стать более дружелюбен к природе. 3. «Каменная ночь. Смерть и память в России XX века» – классический труд Кэтрин Мерридейл, одного из важнейших исследователей мемориальной культуры современности. Автор рассуждает о том, как в имперской, советской и постсоветской России, в эпохи разных типов диктатур и авторитаризма, развивались традиции умирания, похорон, припоминания умерших. Звучит мрачновато, однако книгу обязательно надо читать – это очень внимательное и глубокое наблюдение за теми практиками, которые мы сами довольно долгое время предпочитали игнорировать. 4. «Благоволительницы» – роман Джонатана Лителла, изданный в 2006 году. Сюжет вращается вокруг жизни офицера СС. На протяжении 900 тяжелых страниц автор как будто показывает превращение довольно заурядного человека в преступника. За этим морализаторством проглядывает желание, напротив, отринуть всякие рассуждения о существовании радикального зла. Зло всегда совершается нами, а не какими-то карикатурными, ненормальными негодяями. 5. «Норма» – ключевой роман Владимира Сорокина, квинтэссенция его исследований советского человека. Очень полезен к прочтению сейчас, когда по всему миру снова актуализируются жёсткое «идеологическое воспитание» Перейти к списку Независимая оценка Результаты независимой оценки качества Информационные ресурсы О противодействии коррупции Банк вакансий Другие подборки Все подборки Подборки История, империи и воспоминания Подборка книг от Дениса Шведова подробнее Истерн, зомби и случайные числа Подборка книг от Яны Вагнер подробнее Роуд-муви, самоизоляция и патриархальное общество Подборка книг от Евгении Некрасовой подробнее Смерть, гротеск и духовность Подборка книг для молодежи от Дмитрия Быкова подробнее Гармония классики и современности Подборка книг от Григория Служителя подробнее Цифровая революция, жизнь в лесу, живые и мертвые Подборка книг для молодежи от Галины Юзефович подробнее Молодежный медиацентр Вт – Сб: 13:00 – 21:00 Вс: выходной Пн: выходной 123001, г. Москва, ул. Большая Садовая, д. 1 «Маяковская» 8 (499) 250-51-81 mediateka@svetlovka.ru public@svetlovka.ru - по вопросам партнерства и СМИ Абонемент Вт – Сб: 10:00 – 21:00 Вс: 10:00 – 20:00 Пн: выходной 123001, г. Москва, ул. Садовая-Кудринская, д. 23 стр.1 «Маяковская» 8 (499) 254-62-71 8 (499) 254-84-75 Abonement@svetlovka.ru Детско-юношеский отдел Вт – Сб: 10:00 – 21:00 Вс: 10:00 – 20:00 Пн выходной 123557, г. Москва, пер. Большой Тишинский, д. 24 стр.1 Улица 1905 года Баррикадная Белорусская 8 (499) 254-20-47 deti@svetlovka.ru Государственное бюджетное учреждение культуры города Москвы «Центральная городская молодежная библиотека им.М.А. Светлова» (ГБУК г.Москвы «ЦГМБ им.М.А. Светлова») Каждый последний вторник месяца во всех отделах проводится санитарный день — библиотека закрыта для посетителей. Часы приема директора: Вторник: 16:00 – 18:00 По предварительной записи 8 (495) 650-49-83 biblioteka@svetlovka.ru Мы в социальных сетях: 2003-2022 © Вся информация на сервере представлена исключительно для ознакомительного просмотра ГБУК города Москвы \"Центральная городская молодежная библиотека имени М. А. Светлова\" разработка сайта\n";
            String test2 = "Все курсы О Skillbox О Платформе Центр карьеры Отзывы Контакты Вакансии Школа кураторов Скидки для друзей Вебинары Все вебинары Плейлисты Расписание Медиа Компаниям Войти Образовательная платформа Программирование Дизайн Аналитика Маркетинг Управление Финансы Игры Кино и Музыка Фото Психология Общее развитие Инженерия Английский язык Другое Образовательная платформа №1 по качеству обучения. Вы получите знания, которые помогут вам освоить профессию мечты и изменить жизнь. 518 Курсов 660 Кураторов 549,084 Пользователя Обучение на платформе нацелено на практику: в каждом курсе — только актуальные темы, востребованные навыки и задания для их отработки. Мы регулярно обновляем материалы курсов, помогаем с трудоустройством и стажировкой. Востребованные IT‑профессии Топовые курсы для старта в IT. Вы сможете стать специалистом с нуля, собрать портфолио и начать карьеру через несколько месяцев. Профессия Python-разработчик 12 месяцев Профессия Графический дизайнер 24 месяца Профессия Бизнес-аналитик 13 месяцев Профессия 3D-дженералист 24 месяца Профессия 1С-разработчик 6 месяцев Профессия Инженер по тестированию 12 месяцев Профессия Веб-разработчик 24 месяца Профессия Java-разработчик 9 месяцев Профессия UX/UI-дизайнер 12 месяцев Профессия Специалист по кибербезопасности 24 месяца Профессия Разработчик на C++ 12 месяцев Профессия Frontend-разработчик PRO 12 месяцев Профессия Data Scientist PRO 24 месяца Профессия BI-аналитик 10 месяцев Профессия Веб-дизайнер 24 месяца Профессия Motion-дизайнер 13 месяцев Смотреть всё Профессии Помогают полностью освоить профессию с нуля, собрать портфолио, подготовить резюме и найти работу. 156 профессий Курсы Позволяют получить конкретный навык или изучить инструмент. 362 курса Высшее образование Бакалавриат и магистратура совместно с ведущими вузами России. Диплом государственного образца. 6 программ MBA «Лидеры изменений» Управление в условиях неопределённости для руководителей и собственников бизнеса от Высшей школы менеджмента СПбГУ и Skillbox Подробнее События Skillbox Запись онлайн-конференции От слов к делу: увидеть возможности в новой реальности Смотреть трансляцию Получите профессию в IT Государство оплатит от 50% до 100% стоимости обучения profidigital.gosuslugi.ru 4 шага к переменам в карьере и жизни Изучаете материал на платформе в любое удобное время Общаетесь с экспертами и единомышленниками в Telegram Выполняете практические задания, получаете обратную связь и закрепляете знания Готовите проект и дополняете им своё портфолио Что говорят участники курсов Андрей Жураков Курс «Ableton Live c нуля до PRO» Курс на 100% соответствует своему названию! Я быстро освоил программу, которая раньше своим внешним видом вызывала лишь ужас и полное непонимание. Считаю, что мне очень повезло с куратором. Он очень развёрнуто отвечал на все мои вопросы, подробно рассказывал, что я сделал не совсем правильно — даже прикладывал видео, на которых показывал, как сделать лучше! ВКонтакте Ирина Афанасьева Курс «Профессия Менеджер проектов» Я программист. Долгое время занималась разработкой и внедрением систем, а затем руководила IT-подразделениями крупных энергетических компаний. Сейчас для меня настало время перемен, и я подумала, что нужно обновить знания. Тем более что теоретических основ по управлению проектами у меня не было. Учиться на платформе было очень интересно и познавательно, курсы помогли мне систематизировать весь опыт и получить новые знания. Видео отзыв Алла Комиссаренко Курс «UX-дизайнер с нуля до PRO» Работать дизайнером мне очень нравится, от UX я вообще в восторге, тяга к аналитике у меня была всегда. После долгих поисков работы в новой сфере подруга помогла мне получить заказ на редизайн сайта большой компании. Отдельно хочу сказать спасибо куратору Александру Свободе, он очень подробно расписывал все недочёты и ошибки решений в дизайне. ВКонтакте Людмила Юрченко Курс «HR-менеджер с нуля» Я новичок в профессии, и мне важно было, чтобы курс сочетал в себе не только теорию, но и практику. Свои ощущения я назову более чем положительными, потому что курс мне понравился. Я очень много получила полезной информации, которой буду пользоваться. Сейчас я в процессе поиска работы, и карьерные консультанты Skillbox мне в этом очень помогают. Видео отзыв Зеркаль Михаил Курс «Электронная музыка с нуля до PRO» Курс помог мне систематизировать имеющиеся знания и получить новые. Большое значение сыграл чат с другими участниками курса и кураторами, где всегда можно обсудить любые вопросы, тонкости и нюансы. Но самое главное — курс даёт мотивацию работать над собой, развиваться и позволяет сделать шаг от самиздата в VK или SoundCloud до монетизации творчества и коммерческого продакшна. ВКонтакте Светлана Девянина Курс «Autodesk Maya 2.0» Сама платформа очень удобная, всегда можно задать вопрос в чат. Или написать в Telegram, где участники курса сразу помогут и подскажут. Спикер всегда на связи и записывает видео, если что-то не получается. Мне как новичку материал даётся не сразу, но спикер всё комментирует и разъясняет. Учиться на платформе в целом очень интересно. ВКонтакте Олеся Новикова Курс «Как найти работу сегодня. Экспресс-курс с карьерной консультацией» Я прохожу курс с карьерной консультацией и получила уже три тестовых задания, а на одной вакансии начала проходить испытательный срок \uD83D\uDE0D. Хотя до этого либо получала отказы, либо просто не отвечали работодатели. Для меня волшебство какое-то \uD83D\uDD25. ВКонтакте Готовы к переменам в жизни? Сделайте первый шаг! Витрина курсов Отзывы о Skillbox Смотреть видео Ирина Черкашина Смотреть видео Денис Бобкин Смотреть видео Андрей Ершов Смотреть видео Екатерина Селищева Смотреть видео Михаил Бин Смотреть видео Максим Чиликин Смотреть видео Евгений Федоринов Профориентация: найдите свою идеальную профессию Пройти тест, 5 мин Сотрудничаем с ведущими компаниями Собираем лучшие вакансии в отрасли, готовим к интервью и рекомендуем вас компаниям-партнёрам. 73432880104 78126025210 74952910742 74954453639 74954453936 74954016931 73432880104 79310093246 79310093248 79310093249 78126025210 73512009018 Поможем в выборе! Если у вас есть вопросы о формате или вы не знаете, что выбрать, оставьте свой номер — мы позвоним, чтобы ответить на все ваши вопросы. Имя Телефон Электронная почта Отправить Нажимая на кнопку, я соглашаюсь на обработку персональных данных и с правилами пользования Платформой Направления Программирование Дизайн Маркетинг Управление Игры Кино и Музыка Психология Общее развитие Инженерия Английский язык Другое О Skillbox О Платформе Центр карьеры Отзывы Контакты Вакансии Школа кураторов Проекты Вебинары Медиа Распродажа Сотрудничество Скидки для друзей Партнёрская программа Корпоративным клиентам Работодателям Материалы бренда 8 (800) 500-05-22 Контактный центр +7 499 444 90 36 Отдел заботы о пользователях г. Москва, Ленинский проспект, дом 6, строение 20 hello@skillbox.ru Оферта Оплата Правила пользования Платформой Политика конфиденциальности Премии Рунета 2018, 2019, 2020, 2021 Участник Skolkovo Мы используем файлы cookie, для персонализации сервисов и повышения удобства пользования сайтом. Если вы не согласны на их использование, поменяйте настройки браузера. © Skillbox, 2022\n";
            String test3 = "PlayBack.ru 5 минут от метро ВДНХ 8(495)143-77-71 пн-пт: c 11 до 20 сб-вс: с 11 до 18 Возникла проблема? Напишите нам! Корзина пуста Каталог Экшн-камеры Смартфоны Чехлы для смартфонов Xiaomi Защитные стекла для смартфонов Xiaomi Чехлы для Huawei/Honor Чехлы для смартфонов Samsung Защитные стекла для смартфонов Samsung Планшеты Зарядные устройства и кабели Держатели для смартфонов Автодержатели Носимая электроника Наушники и колонки Гаджеты Xiaomi Запчасти для телефонов Чехлы для планшетов Аксессуары для фото-видео Чехлы для смартфонов Apple Товары для автомобилистов USB Флеш-накопители Товары для детей Чехлы для смартфонов Doogee Защитные стекла для смартфонов Realme Чехлы для смартфонов Realme Карты памяти Защитные стекла для планшетов Защитные стекла для смартфонов Доставка Самовывоз Оплата Гарантия и обмен Контакты ◄ Все смартфоны xiaomi Артикул: 111100 Смартфон Xiaomi Redmi 9A 2/32 ГБ Global, синий 7360р. Положить в корзину Есть на складе При оплате картой (возможна только при самовывозе): 7654 р. Вы можете заказать доставку товара, либо забрать его самостоятельно из пункта самовывоза Модели, похожие по параметрам: Смартфон Xiaomi Redmi 9A 2/32 ГБ Global, зеленый (Артикул: 111102) 6970р. Подробнее Смартфон Xiaomi Redmi 9A 2/32 ГБ Global, темно-серый (Артикул: 111101) 6950р. Подробнее Смартфон Xiaomi Redmi 9A 2/32 ГБ RU, зеленый (Артикул: 111165) 7800р. Подробнее Смартфон Xiaomi Redmi 9A 2/32 ГБ RU, синий (Артикул: 111167) 7700р. Подробнее Смартфон Xiaomi Redmi 9A 2/32 ГБ RU, темно-серый (Артикул: 111166) 7700р. Подробнее Все аксессуары для Xiaomi Redmi 9A 2/32 ГБ Global, синий Скрыть/показать Защитные стекла для смартфонов Redmi 9A/Redmi 9C Защитное стекло 3D для Xiaomi Redmi 9A/Redmi 9C (Артикул: 111205) 550р. Купить Защитное стекло 5D Monarch для Redmi 9A/Redmi 9C (Артикул: 111259) 700р. Купить Защитное стекло 4D Monarch для Redmi 9A/Redmi 9C (Артикул: 112306) 650р. Купить Защитное противоударное стекло Jasper Mr. Perfect для Xiaomi Redmi 9A/Redmi 9C (Артикул: 112444) 600р. Купить Чехлы для Redmi 9A Накладка силиконовая Monarch Elegant Design MT-03 для Xiaomi Redmi 9A Синяя (Артикул: 111202) 600р. Купить Накладка на заднюю панель силиконовая Monarch Premium PS-01 для Xiaomi Redmi 9A Синяя (Артикул: 111310) 700р. Купить Накладка на заднюю панель силиконовая Monarch Premium PS-01 для Xiaomi Redmi 9A Розовая (Артикул: 111314) 700р. Купить Накладка на заднюю панель силиконовая Monarch Premium PS-01 для Xiaomi Redmi 9A Оранжевая (Артикул: 111321) 700р. Купить Чехол-накладка Silicone Case для Xiaomi Redmi 9A Черный (Артикул: 112760) 500р. Купить Чехол книжка Fashion Case Retro Line для Xiaomi Redmi 9A Красная (Артикул: 1121552) 700р. Купить Накладка силиконовая Monarch Elegant Design MT-03 для Xiaomi Redmi 9A Красная (Артикул: 1121671) 600р. Купить Чехол-накладка с карманом-визитницей ILEVEL для Xiaomi Redmi 9A Черный (Артикул: 1121683) 600р. Купить Чехол силиконовый ультратонкий DF xiCase-56 для Xiaomi Redmi 9A Прозрачный (Артикул: 1121713) 500р. Купить Чехол книжка DF xiFlip-63 для Xiaomi Redmi 9A Черный (Артикул: 1121714) 800р. Купить Чехол книжка DF xiFlip-63 для Xiaomi Redmi 9A Синий (Артикул: 1121820) 800р. Купить Чехол силиконовый с блестками DF xiShine-01 для Xiaomi Redmi 9A (Артикул: 1121846) 600р. Купить Накладка силиконовая с микрофиброй DF xiOriginal-13 для Xiaomi Redmi 9A Черная (Артикул: 1121847) 700р. Купить Накладка на заднюю панель силиконовая Monarch Premium PS-01 для Xiaomi Redmi 9A Пудровая (Артикул: 1121911) 700р. Купить Накладка силиконовая с текстурой карбон для Xiaomi Redmi 9A Черная (Артикул: 1122369) 700р. Купить Чехол книжка Protective Case для Xiaomi Redmi 9A Красная (Артикул: 1122370) 700р. Купить Чехол книжка Protective Case для Xiaomi Redmi 9A Розовая (Артикул: 1122371) 700р. Купить Проводные наушники Наушники Xiaomi 1More Piston Fit-In-Ear Silver (Артикул: 107899) 1200р. Купить Наушники Xiaomi 1More Piston Fit-In-Ear Teal (Артикул: 107900) 1200р. Купить Наушники Xiaomi 1More Piston Fit-In-Ear Space Gray (Артикул: 107901) 1200р. Купить Наушники Xiaomi 1More Piston Fit-In-Ear Pink (Артикул: 108166) 1200р. Купить Наушники Maimi H16 (Артикул: 110549) 500р. Купить Наушники Maimi H13 (Артикул: 110550) 600р. Купить Наушники 1More Piston In-Ear Earphone 1M301 Черные (Артикул: 112550) 1990р. Купить Наушники Awei ES-10TY Серые (Артикул: 1121834) 900р. Купить Наушники Awei ES-30TY Черные (Артикул: 1121835) 900р. Купить Наушники Awei ES390i Черные (Артикул: 1121836) 900р. Купить Вакуумные наушники Awei L1 Super Bass Черные (Артикул: 1121838) 1000р. Купить Наушники Hoco M64 (Артикул: 1122618) 650р. Купить Наушники Hoco M64 (Артикул: 1122619) 650р. Купить Автодержатели Магнитный автодержатель для смартфона Magtach Белый (Артикул: 106438) 250р. Купить Магнитный автомобильный держатель Hoco CA23 (Артикул: 107573) 1200р. Купить Магнитный автомобильный держатель Hoco CA28 Черный (Артикул: 107876) 1300р. Купить Автомобильный держатель Borofone BH1 (Артикул: 109146) 650р. Купить Магнитный автомобильный держатель Hoco CA67 Черный (Артикул: 111163) 800р. Купить Магнитный автомобильный держатель Hoco CA75 Черный (Артикул: 112371) 2100р. Купить Автомобильный держатель с беспроводной быстрой зарядкой Baseus Rock-solid Electric (WXHW01-B0S) (Артикул: 112436) 2800р. Купить Автомобильный держатель с беспроводной быстрой зарядкой Baseus Explore Wireless Charger Gravity Car Mount (WXYL-K01) (Артикул: 112439) 1650р. Купить Автомобильный держатель с беспроводной быстрой зарядкой ZMI WCJ10 (Артикул: 112533) 1990р. Купить Автомобильный держатель Hoco CA40 Черный (Артикул: 112747) 600р. Купить Беспроводные наушники Беспроводные Bluetooth наушники Haylou T19 Белые (Артикул: 111346) 5100р. Купить Беспроводные Bluetooth наушники Meizu POP2 Белые (Артикул: 111369) 4200р. Купить Беспроводные наушники Haylou MoriPods White (Артикул: 112951) 2200р. Купить Беспроводные наушники Skullcandy Sesh Boost True Wireless In-Ear (S2TVW-N741) Deep red (Артикул: 1121326) 4500р. Купить Беспроводные наушники Xiaomi Redmi Buds 3 Pro Черные (Артикул: 1121933) 5000р. Купить Беспроводные наушники Vivo TWS 2e (Артикул: 1122411) 4800р. Купить Беспроводные наушники 1More PistonBuds Pro Black (EC302) (Артикул: 1122647) 3700р. Купить Беспроводные наушники 1More PistonBuds Pro White (EC302) (Артикул: 1122648) 3700р. Купить Беспроводные наушники Haylou GT7 (Артикул: 1122655) 2100р. Купить Беспроводные наушники Haylou GT7 (Артикул: 1122656) 2100р. Купить Беспроводные наушники Haylou W1 (Артикул: 1122657) 3200р. Купить Беспроводные наушники Haylou GT6 (Артикул: 1122658) 2200р. Купить Автомобильные зарядные устройства ZMI Car Charger AP821 (Артикул: 110243) 1140р. Купить Автомобильное зарядное устройство Hoco NZ1 Черный (Артикул: 1122420) 1200р. Купить Автомобильное зарядное устройство Hoco NZ1 Серый металлик (Артикул: 1122421) 1200р. Купить Автомобильное зарядное устройство ZMI Metal Car Charger AP721 (Артикул: 1122668) 1100р. Купить Сетевые зарядные устройства Сетевое зарядное устройство Borofone BA17A Centrino (Артикул: 109194) 900р. Купить Зарядное устройство Borofone BA20A Черное (Артикул: 110263) 400р. Купить Зарядное устройство Borofone BA46A Черное (Артикул: 112363) 920р. Купить Сетевая зарядка Baseus GaN Mini Quick Charger C+U (CCGAN-QO1) Черная (Артикул: 1121840) 2800р. Купить Сетевое зарядное устройство Borofone BA38A Plus Белое (Артикул: 1121844) 900р. Купить Зарядное устройство Borofone BA56A Lavida USB-Type C (Артикул: 1122298) 1200р. Купить Зарядное устройство Borofone BA57A Easy Speed USB-Type C (Артикул: 1122299) 1400р. Купить Зарядное устройство Hoco C12Q Smart (Артикул: 1122300) 900р. Купить Зарядное устройство Hoco N19 Type-C (Артикул: 1122301) 1400р. Купить Зарядное устройство Borofone BA47A Белое (Артикул: 1122744) 900р. Купить Сетевое зарядное устройство Hoco N13 Bright (Артикул: 1122745) 1600р. Купить Сетевое зарядное устройство Hoco N13 Bright (Артикул: 1122746) 1600р. Купить Универсальные внешние аккумуляторы Внешний аккумулятор Xiaomi Redmi Power Bank 10000 mAh (PB100LZM) (Артикул: 110107) 1250р. Купить Внешний аккумулятор Xiaomi Mi Power Bank 3 10000mAh (PLM13ZM) Черный (Артикул: 110799) 1500р. Купить Внешний аккумулятор Xiaomi Redmi Power Bank Fast Charge 20000 mAh Черный (Артикул: 112664) 2990р. Купить Внешний аккумулятор Xiaomi Mi Power Bank 3 Ultra compact 10000mAh (BHR4412GL) Черный (Артикул: 1121540) 1550р. Купить Кабели Aux Аудио кабель Hoco UPA03 (Артикул: 107419) 350р. Купить Aux Аудио кабель Borofone BL4 Серый (Артикул: 110980) 400р. Купить Кабель Hoco X35 Micro USB Черный (Артикул: 111013) 500р. Купить Кабель Hoco X20 Micro USB (1m) Черный (Артикул: 1121914) 700р. Купить Кабель Hoco X20 Micro USB (1m) Белый (Артикул: 1121915) 700р. Купить Кабель Hoco X20 Micro USB (2m) Черный (Артикул: 1121932) 800р. Купить Кабель Borofone BX51 USB на Micro USB (Артикул: 1122358) 600р. Купить Моноподы Монопод (палка для селфи, телефона, фотоаппарата, видеокамеры) Jmary QP-128 Белый (Артикул: 103394) 550р. Купить Монопод (палка для селфи, телефона, фотоаппарата, видеокамеры) Jmary QP-168 Белый (Артикул: 103409) 550р. Купить Монопод Xiaomi Mi Bluetooth Selfie Stick (LYZPG01YM) Черный (Артикул: 108079) 1300р. Купить Штатив-монопод SelfieCom K08 c Bluetooth управлением Черный (Артикул: 110411) 550р. Купить Штатив-трипод Xiaomi Mi Selfie Stick c Bluetooth управлением Черный (XMZPG05YM) (Артикул: 1121891) 1650р. Купить Переходники XR5 NANO sim adapter (Артикул: 100191) 100р. Купить Фитнес браслеты и умные часы Умные часы Haylou LS05 Smart Watch Solar Черные (Артикул: 111358) 3000р. Купить Фитнес-браслет Xiaomi Mi Band 5 (Global) (Артикул: 112329) 2450р. Купить Фитнес-браслет Xiaomi Mi Band 6 Black (Артикул: 1121434) 2950р. Купить Карты памяти Карта памяти Sandisk Ultra microSDHC Class 10 UHS-I 80MB/s 32GB (SDSQUNS-032G-GN3MN) (Артикул: 112675) 1350р. Купить Карта памяти MicroSDXC 64 Гб Samsung EVO Plus (Артикул: 1121972) 2000р. Купить Карта памяти Kingston SDCS2 256 GB (Артикул: 1122401) 3970р. Купить Карта памяти Sandisk Ultra microSDXC Class 10 UHS-I 100MB/s + SD adapter 128 GB (Артикул: 1122402) 2440р. Купить Карта памяти MicroSDXC 128 Гб Samsung EVO Plus 128 ГБ (MB-MC128KA/RU) (Артикул: 1122403) 2560р. Купить Держатели для смартфонов Кольцо-держатель для телефона RING HOLDER Magnetic (Артикул: 1121964) 400р. Купить Кольцо-держатель MI для телефона (Артикул: 1121965) 400р. Купить Описание Иммерсивный 6.53\" HD+ дисплей. Большой экран обеспечивает ощущение полного погружения в виртуальный мир. Защита от синего излучения для большего комфорта. Специальная технология обеспечивает защиту глаз от синего излучения и снижает нагрузку на них. Аккумулятор 5000 мА·ч. До 34 дней в режиме ожидания. Аккумулятор, который способен на многое. Невероятный ресурс аккумулятора. Аккумулятор рассчитан на 1000 циклов зарядки – вы сможете пользоваться смартфоном 2,5 года без заметных потерь ёмкости. Достойная производительность. Процессор MediaTek Helio G25 обеспечивает отличную производительность и плавность в работе. HyperEngine Продвинутая технология HyperEngine обеспечивает плавность и отзывчивость в играх, а также улучшенное качество соединения и реалистичную графику. Основная камера с ИИ. Сохраните все самые важные и яркие моменты жизни с 13 мегапиксельной основной камерой. Вскружите реальность. Создавайте креативные видео с эффектом «Калейдоскоп» и удивляйте друзей. Фронтальная камера с функцией «Улучшение с ИИ». Ваша красота в центре внимания. Функция «Спуск ладонью». Теперь нет ничего проще, чем групповые селфи. Просто покажите ладонь, чтобы запустить таймер съёмки. Разблокировка по лицу с ИИ. Быстрая и удобная разблокировка. Технология распознавания лиц делает разблокировку устройства ещё проще. Новый дизайн. Специальная текстура задней панели, которая защищает устройство от появления отпечатков пальцев. Технические характеристики Общие характеристики Тип смартфон Операционная система Android Тип корпуса классический Количество SIM-карт 2 Тип SIM-карты nano SIM Режим работы нескольких SIM-карт попеременный Вес 194 г Размеры (ШxВxТ) 77.07x164.9x9 мм Экран Тип экрана цветной, сенсорный Тип сенсорного экрана мультитач, емкостный Диагональ 6.53 дюйм. Размер изображения 1600x720 Число пикселей на дюйм (PPI) 269 Соотношение сторон 20:9 Автоматический поворот экрана есть Мультимедийные возможности Количество основных (тыловых) камер 1 Разрешения основных (тыловых) камер 13 МП Фотовспышка тыльная, светодиодная Функции основной (тыловой) фотокамеры автофокус Запись видеороликов есть Макс. разрешение видео 1920x1080 Макс. частота кадров видео 60 кадров/с Частота кадров при записи видео Full HD 60 кадров/с Фронтальная камера есть, 5 МП Аудио MP3, AAC, WAV, WMA, FM-радио Разъем для наушников mini jack 3.5 mm Связь Стандарт GSM 900/1800/1900, 3G, 4G LTE, VoLTE Интерфейсы Wi-Fi 802.11n, Wi-Fi Direct, Bluetooth 5.0, USB Геопозиционирование BeiDou, A-GPS, ГЛОНАСС, GPS Память и процессор Процессор MediaTek Helio G25 Количество ядер процессора 8 Видеопроцессор PowerVR GE8320 Объем встроенной памяти 32 Гб Объем оперативной памяти 2 Гб Слот для карт памяти есть, объемом до 512 ГБ, отдельный Питание Емкость аккумулятора 5000 мА⋅ч Тип разъема для зарядки micro-USB Другие функции Громкая связь (встроенный динамик) есть Управление голосовой набор, голосовое управление Режим полета есть Датчики освещенности, приближения, разблокировка по лицу Фонарик есть Информация Наши спецпредложения Доставка Оплата Гарантия Контакты Положение о конфиденциальности и защите персональных данных +7(495)143-77-71 График работы: пн-пт: c 11-00 до 20-00 сб-вс: с 11-00 до 18-00 Наш адрес: Москва, Звездный бульвар, 10, строение 1, 2 этаж, офис 10. 2005-2022 ©Интернет магазин PlayBack.ru Наверх\n";
//            String eng = "Skillbox";
//            String ru = "создание";
//            Pattern english = Pattern.compile("([A-z]+)");
//            Pattern russian = Pattern.compile("([А-яё]+)");
//            System.out.println(Pattern.matches(russian.pattern(), ru));
            LemmaCounter counter = new LemmaCounter(test3);
            counter.getWordsMap().forEach((key, value) -> System.out.println(key + "\t" + value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
