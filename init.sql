--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5 (Debian 17.5-1.pgdg120+1)
-- Dumped by pg_dump version 17.5

-- Started on 2025-07-02 12:53:23

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3430 (class 1262 OID 16384)
-- Name: plac; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE plac WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


\connect plac

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 16389)
-- Name: address; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.address (
    id integer NOT NULL,
    client_id bigint,
    street character varying(255),
    home character varying(255),
    local character varying(255),
    city character varying(255),
    post character varying(255),
    country character varying(255)
);


--
-- TOC entry 218 (class 1259 OID 16394)
-- Name: address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.address_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3431 (class 0 OID 0)
-- Dependencies: 218
-- Name: address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.address_id_seq OWNED BY public.address.id;


--
-- TOC entry 219 (class 1259 OID 16395)
-- Name: client; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.client (
    id integer NOT NULL,
    address_id bigint,
    email character varying(255),
    phone character varying(255),
    name character varying(255),
    nip character varying(255),
    krs character varying(255),
    active boolean NOT NULL,
    need_invoice boolean NOT NULL,
    person_to_contact character varying(255),
    salutation character varying(255),
    products numeric[]
);


--
-- TOC entry 220 (class 1259 OID 16400)
-- Name: client_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.client_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3432 (class 0 OID 0)
-- Dependencies: 220
-- Name: client_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.client_id_seq OWNED BY public.client.id;


--
-- TOC entry 221 (class 1259 OID 16401)
-- Name: dump_plac; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dump_plac (
    "--" character varying(128)
);


--
-- TOC entry 222 (class 1259 OID 16404)
-- Name: invoice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoice (
    id integer NOT NULL,
    date date,
    main_account smallint,
    number character varying(255),
    price numeric(38,2),
    price_with_tax numeric(38,2),
    tax numeric(38,2),
    tax_account smallint,
    client_id bigint,
    products_invoice numeric[],
    send_date date
);


--
-- TOC entry 223 (class 1259 OID 16409)
-- Name: invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.invoice_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3433 (class 0 OID 0)
-- Dependencies: 223
-- Name: invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.invoice_id_seq OWNED BY public.invoice.id;


--
-- TOC entry 224 (class 1259 OID 16410)
-- Name: product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product (
    id integer NOT NULL,
    price numeric(38,2),
    price_with_vat numeric(38,2),
    product_enum smallint,
    quantity numeric(38,2),
    unit_price numeric(38,2),
    vat_amount numeric(38,2),
    vat_rate integer NOT NULL,
    client_id bigint,
    products_description numeric[]
);


--
-- TOC entry 225 (class 1259 OID 16415)
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3434 (class 0 OID 0)
-- Dependencies: 225
-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_id_seq OWNED BY public.product.id;


--
-- TOC entry 226 (class 1259 OID 16416)
-- Name: product_invoice_dto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_invoice_dto (
    id integer NOT NULL,
    price numeric(38,2),
    price_with_vat numeric(38,2),
    product_enum smallint,
    quantity numeric(38,2),
    unit_price numeric(38,2),
    vat_amount numeric(38,2),
    vat_rate integer NOT NULL,
    client_id bigint,
    invoice_id bigint
);


--
-- TOC entry 227 (class 1259 OID 16419)
-- Name: product_invoice_dto_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_invoice_dto_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3435 (class 0 OID 0)
-- Dependencies: 227
-- Name: product_invoice_dto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_invoice_dto_id_seq OWNED BY public.product_invoice_dto.id;


--
-- TOC entry 228 (class 1259 OID 16420)
-- Name: productdescription; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.productdescription (
    id integer NOT NULL,
    product_id bigint,
    client_id bigint,
    number character varying(255),
    location character varying(255),
    description character varying(255),
    acquisition date,
    maintencedone date[]
);


--
-- TOC entry 229 (class 1259 OID 16425)
-- Name: productdescription_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.productdescription_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3436 (class 0 OID 0)
-- Dependencies: 229
-- Name: productdescription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.productdescription_id_seq OWNED BY public.productdescription.id;


--
-- TOC entry 230 (class 1259 OID 16426)
-- Name: user_login; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_login (
    id integer NOT NULL,
    username character varying(255),
    password character varying(255),
    role character varying(255),
    account_expired boolean,
    account_locked boolean,
    credentials_expired boolean,
    enabled boolean,
    company_nip character varying(255),
    company_id integer,
    first_name character varying(255),
    last_name character varying(255),
    email character varying(255),
    tel character varying(255)
);


--
-- TOC entry 231 (class 1259 OID 16431)
-- Name: user_login_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_login_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3437 (class 0 OID 0)
-- Dependencies: 231
-- Name: user_login_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_login_id_seq OWNED BY public.user_login.id;


--
-- TOC entry 3244 (class 2604 OID 16432)
-- Name: address id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.address ALTER COLUMN id SET DEFAULT nextval('public.address_id_seq'::regclass);


--
-- TOC entry 3245 (class 2604 OID 16433)
-- Name: client id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client ALTER COLUMN id SET DEFAULT nextval('public.client_id_seq'::regclass);


--
-- TOC entry 3246 (class 2604 OID 16434)
-- Name: invoice id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice ALTER COLUMN id SET DEFAULT nextval('public.invoice_id_seq'::regclass);


--
-- TOC entry 3247 (class 2604 OID 16435)
-- Name: product id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product ALTER COLUMN id SET DEFAULT nextval('public.product_id_seq'::regclass);


--
-- TOC entry 3248 (class 2604 OID 16436)
-- Name: product_invoice_dto id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_invoice_dto ALTER COLUMN id SET DEFAULT nextval('public.product_invoice_dto_id_seq'::regclass);


--
-- TOC entry 3249 (class 2604 OID 16437)
-- Name: productdescription id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productdescription ALTER COLUMN id SET DEFAULT nextval('public.productdescription_id_seq'::regclass);


--
-- TOC entry 3250 (class 2604 OID 16438)
-- Name: user_login id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_login ALTER COLUMN id SET DEFAULT nextval('public.user_login_id_seq'::regclass);


--
-- TOC entry 3410 (class 0 OID 16389)
-- Dependencies: 217
-- Data for Name: address; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.address VALUES (1, 1, 'Kasztanowa', '18-20', '115', 'Wroclaw', '', 'PL');
INSERT INTO public.address VALUES (3, 3, 'Borowska', '266', '4', 'Wroclaw', '50-558', 'PL');
INSERT INTO public.address VALUES (4, 4, 'Jerzmanowska', '127A', NULL, 'Wroclaw', '54-530', 'PL');
INSERT INTO public.address VALUES (8, 8, 'Zwycieska', '12B', '7', 'Wroclaw', '53-033', 'PL');
INSERT INTO public.address VALUES (10, 10, '1 MAJA', '30A', NULL, 'Opole', '45-355', 'PL');
INSERT INTO public.address VALUES (12, 12, 'Grabiszynska', '279', '14', 'Wroclaw', '53-234', 'PL');
INSERT INTO public.address VALUES (16, 16, 'Wolbromska', '18', '1B', 'Wroclaw', '53-148', 'PL');
INSERT INTO public.address VALUES (48, 50, 'ul. Owcza', '31', '', 'Szczecin', '70-795', 'PL');
INSERT INTO public.address VALUES (36, 38, '', '28', '', 'Szklarka Myślniewska', '63-500', 'PL');
INSERT INTO public.address VALUES (51, 53, 'Wilków', '69', '', 'Wilków', '24-313', 'PL');
INSERT INTO public.address VALUES (37, 39, 'Drukarska', '14a', '4', 'Wrocław', '53-311', 'Polska');
INSERT INTO public.address VALUES (7, 7, 'Szczesliwa', '26', '', 'Warszawa', '02-454', 'PL');
INSERT INTO public.address VALUES (17, 17, 'Józefa Rostafińskiego', '5', '2', 'WrocĹ‚aw', '50-247', 'PL');
INSERT INTO public.address VALUES (6, 6, 'marsz. Jozefa Pilsudskiego', '266', '4', 'Wroclaw', '50-020', 'PL');
INSERT INTO public.address VALUES (11, 11, 'Jajczarska', '24', '', 'Wroclaw', '54-008', 'PL');
INSERT INTO public.address VALUES (5, 5, 'marsz. Jozefa Pilsudskiego', '74', '320', 'Wroclaw', '50-020', 'PL');
INSERT INTO public.address VALUES (20, 22, 'Stanisława Kunickiego', '57', '8', 'Wrocław', '54-616', 'PL');
INSERT INTO public.address VALUES (39, 41, 'Kręta', '17', '4', 'JELENIA GÓRA', '58-570', 'PL');
INSERT INTO public.address VALUES (18, 18, 'Przebendowskiego', '21', '34', 'Puck', '84-100', 'PL');
INSERT INTO public.address VALUES (13, 13, 'Graniczna', '133', '', 'Wroclaw', '54-530', 'PL');
INSERT INTO public.address VALUES (40, 42, 'Olszówka', '27A', '', 'Bielsko-Biała', '43-309', 'PL');
INSERT INTO public.address VALUES (9, 9, 'Rogowska', '127', '', 'Wroclaw', '54-440', 'PL');
INSERT INTO public.address VALUES (21, 23, 'ppp', 'ppp', '1', '1', '53-238', 'Polska');
INSERT INTO public.address VALUES (41, 43, 'Warta Bolesławiecka', '60e', '', 'Warta Bolesławiecka', '59-720', 'PL');
INSERT INTO public.address VALUES (15, 15, 'Wolbromska', '18', '1B', 'Wrocław', '53-148', 'PL');
INSERT INTO public.address VALUES (14, 14, 'Tadeusza Kościuszki', '86', '22', 'Wroclaw', '50-441', 'PL');
INSERT INTO public.address VALUES (42, 44, 'pl. Legionów', '4', '9', 'Wrocław', '50-047', 'PL');
INSERT INTO public.address VALUES (43, 45, 'Rymarska', '28', '5', 'Wrocław', '53-206', 'PL');
INSERT INTO public.address VALUES (44, 46, 'Nowodworska', '101', '20', 'Wrocław', '54-438', 'PL');
INSERT INTO public.address VALUES (22, 24, 'Inowrocławska', '50', '1', 'Wrocław', '55-555', 'PL');
INSERT INTO public.address VALUES (45, 47, 'TADEUSZA ZIELIŃSKIEGO', '32', '53', 'Wrocław', '53-534', 'PL');
INSERT INTO public.address VALUES (23, 25, 'xxx', '1', '', 'Wrocław', '11-11', 'PL');
INSERT INTO public.address VALUES (24, 26, 'brak', '1', '', 'Duszniki-Zdrój', '00-000', 'PL');
INSERT INTO public.address VALUES (25, 27, 'Bezpieczna', '37', '', 'Wrocław', '00-000', 'PL');
INSERT INTO public.address VALUES (26, 28, 'Hallera', '69', '', 'Wrocław', '00-000', 'PL');
INSERT INTO public.address VALUES (27, 29, 'nieznana', '0', '', 'Zgorzelec', '00-000', 'PL');
INSERT INTO public.address VALUES (28, 30, 'nieznana', '0', '', 'Świdnica', '00-000', 'PL');
INSERT INTO public.address VALUES (29, 31, 'Pereca', '46', '1', 'Wrocław', '53-430', 'PL');
INSERT INTO public.address VALUES (30, 32, 'Główna', '67', '', 'Wrocław', '54-061', 'PL');
INSERT INTO public.address VALUES (31, 33, 'Powstanców Śląskich', '52', '33', 'Wrocław', '00-000', 'PL');
INSERT INTO public.address VALUES (32, 34, 'Tęczowa', '83h', '20', 'Wrocław', '00-000', 'PL');
INSERT INTO public.address VALUES (33, 35, 'Ostrowskiego', '102', '', 'Wrocław', '53-238', 'PL');
INSERT INTO public.address VALUES (34, 36, 'Nowowiejska', '103', '2', 'Wrocław', '00-000', 'PL');
INSERT INTO public.address VALUES (35, 37, 'Ostrowskiego', '102', '', 'Wrocław', '53-238', 'PL');
INSERT INTO public.address VALUES (46, 48, 'żelazna', '49', '14', 'Wrocław', '53-427', 'PL');
INSERT INTO public.address VALUES (2, 2, 'Złotostocka', '27', '9', 'Wroclaw', '50-511', 'PL');
INSERT INTO public.address VALUES (47, 49, 'NIEDŹWIEDZIA ', '44', '1', 'WROCŁAW', '54-210', 'PL');
INSERT INTO public.address VALUES (38, 40, 'Tęczowa', '91', '14', 'Wrocław', '', 'PL');
INSERT INTO public.address VALUES (50, 52, 'PL. GEN. WALEREGO WRÓBLEWSKIEGO', '3A', '2', 'Wrocław', '50-413', 'PL');
INSERT INTO public.address VALUES (52, 54, 'Stanisława Leszczyńskiego', '4', '29', 'Wrocław', '50-078', 'PL');
INSERT INTO public.address VALUES (53, 55, 'oś. Złote', '9i', '', 'Dzierżoniów', '58-200', 'PL');
INSERT INTO public.address VALUES (54, 56, 'Letnia', '1', '', 'Wrocław', '53-018', 'PL');
INSERT INTO public.address VALUES (55, 57, 'Józefa Marszałka Piłsudskiego', '74', '320', 'Wrocław', '50-020', 'PL');
INSERT INTO public.address VALUES (56, 58, 'ŚW. MIKOŁAJA', '30/31', '1C', 'Wrocław', '50-128', 'PL');
INSERT INTO public.address VALUES (57, 59, 'Legnicka', '134', '10', 'Wrocław', '54-206', 'PL');
INSERT INTO public.address VALUES (49, 51, 'Marcelego Nenckiego', '31', '', 'Wrocław', '52-223', 'PL');


--
-- TOC entry 3412 (class 0 OID 16395)
-- Dependencies: 219
-- Data for Name: client; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.client VALUES (1, 1, 'biuro@cedru.pl', '', 'CEDRU sp z o o', '8971780140', NULL, true, true, 'Artur', 'Pani Magdo', NULL);
INSERT INTO public.client VALUES (3, 3, 'sylwia.czemarnik@gmail.com', 'phone', 'STJ Wroclaw', '8982257152', NULL, true, true, 'test1', 'Pani Sylwio', NULL);
INSERT INTO public.client VALUES (4, 4, 'aceco@gazeta.pl', 'phone', 'ACECO Wojciech Szewczyk ', '5651203445', NULL, true, true, 'Wojciech', 'Panie Wojtku', NULL);
INSERT INTO public.client VALUES (8, 8, 'utmlogistik@gmail.com', 'phone', 'APPA Joanna Karlinska', '7431145511', NULL, true, true, 'mÄ…ĹĽ', 'Pani Joanno', NULL);
INSERT INTO public.client VALUES (10, 10, 'nazaruktaras89@gmail.com', 'phone', 'PHEONIX CAPITAL GROUP sp. zoo', '7543342582', NULL, true, true, 'Takes', NULL, NULL);
INSERT INTO public.client VALUES (12, 12, 'krzychuszkarlat@wp.pl', 'phone', 'SZKARLAT BUDOWNICTWO Sp Z O.O.', '8851638830', NULL, true, true, 'Krzysztof', 'Panie Krzysztofie', NULL);
INSERT INTO public.client VALUES (16, 16, 'ORESTZHYLA@WP.PL', 'phone', 'Wromar', '8943191058', NULL, true, false, 'OREST ZHYLA', 'Panie Orest', NULL);
INSERT INTO public.client VALUES (38, 36, 'bm1201@tlen.pl', NULL, 'MYSZKOWSKI BENEDYKT', '6221119323', '', true, true, 'Benek', 'Panie Benedykcie', NULL);
INSERT INTO public.client VALUES (22, 20, 'rozinski@o2.pl', NULL, 'THE GREAT Marcin Roziński', '6131468190', '', true, true, 'Marcin', 'Panie Marcinie', NULL);
INSERT INTO public.client VALUES (49, 47, 'asiapirog18@gmail.com', NULL, 'ZOO TEAM sp. z o.o.', '8943161844', '0000874091', true, true, 'Joanna', 'Pani Joanno', NULL);
INSERT INTO public.client VALUES (18, 18, 'scaftechpb@gmail.com', NULL, 'SCAFTECH PATRYK BOCHEN', '5871666696', '', true, true, 'Patryk', 'Panie Patryku', NULL);
INSERT INTO public.client VALUES (42, 40, 'pssspzoo@gmail.com', NULL, 'PSS sp. zoo', '5472230856', '0000968544', true, true, 'Rafał', 'Panie Rafale', NULL);
INSERT INTO public.client VALUES (13, 13, 'piotrzuko@wp.pl', NULL, 'ZUKO Piotr Koba', '8941081549', '', false, true, 'Piotr', 'Panie Piotrze', NULL);
INSERT INTO public.client VALUES (24, 22, 'kerujek20@gmail.com', NULL, 'Jerzy Kasyk', '6202200238', '', true, false, 'Jerzy', 'Panie Jerzy', NULL);
INSERT INTO public.client VALUES (9, 9, 'a.sikora@wpbsikora.com', NULL, 'WPB Sikora', '8943156493', '', false, true, 'Siergiej', '', NULL);
INSERT INTO public.client VALUES (39, 37, 'yukhim82@gmail.com', NULL, 'Vitalii Iukhym', 'GA673654', '', true, false, 'Vitalii', 'Panie Vitalii', NULL);
INSERT INTO public.client VALUES (25, 23, 'marek@kasyk.pl', NULL, 'Marek Kasyk', '1111111111', '', true, false, 'Marek / Jerzy', 'Panie Marku', NULL);
INSERT INTO public.client VALUES (15, 15, 'elnalaz@gmail.com', NULL, 'MARCIN NALAZEK', '6121800458', '', true, true, 'Marcin', 'Panie Marcinie', NULL);
INSERT INTO public.client VALUES (26, 24, 'damian.klimowicz80@gmail.com', NULL, 'Damian Klimowicz', '80072315471', '', true, false, 'Damian', 'Panie Damianie', NULL);
INSERT INTO public.client VALUES (14, 14, 'jagodka-jagodka@o2.pl', NULL, 'MIŚ-TRANS MATEUSZ MIŚKIEWICZ', '8992767015', '', true, true, 'Mateusz', 'Panie Złoty', NULL);
INSERT INTO public.client VALUES (52, 50, 'VIANPOL.EU@GMAIL.COM', NULL, 'VIANPOL', '8992930923', '', true, true, 'Galyna', 'Pani Galyno', NULL);
INSERT INTO public.client VALUES (27, 25, 'brak@maila.pl', NULL, 'Bennyko Ykpaiha', 'FH417387', '', true, false, 'Yurii', 'Panie Yurii', NULL);
INSERT INTO public.client VALUES (28, 26, 'bram@maila.pl', NULL, 'Andrzej Przewoźniak', '64110605935', '', true, false, 'Andrzej', 'Panie Andrzeju', NULL);
INSERT INTO public.client VALUES (29, 27, 'wojtasdy@gmail.com', NULL, 'Wojciech Dychała', '91071007537', '', true, false, 'Wojtek', 'Panie Wojciechu', NULL);
INSERT INTO public.client VALUES (30, 28, 'icebula10@gmail.com', NULL, 'Ireneusz Cebula', '70070307855', '', true, false, 'Irek', 'Panie Ireneuszu', NULL);
INSERT INTO public.client VALUES (31, 29, 'sedziakpawel88@gmail.com', NULL, 'Paweł Sędziak', '88050905834', '', true, false, 'Paweł', 'Panie Pawle', NULL);
INSERT INTO public.client VALUES (32, 30, 'paw.drz@o2.pl', NULL, 'Paweł Drzewiecki', '68070601127', '', true, false, 'Paweł', 'Panie Pawle', NULL);
INSERT INTO public.client VALUES (33, 31, 'trypka@brakmaila.pl', NULL, 'Mariusz Trypka', '87092213916', '', true, false, 'Mariusz', 'PanieMariuszu', NULL);
INSERT INTO public.client VALUES (34, 32, 'psharan96@gmail.com', NULL, 'Manpreet Singh', '90091218110', '', true, false, 'Manpreet ', 'Panie Manpreet', NULL);
INSERT INTO public.client VALUES (35, 33, 'kstefanko77@gmail.com', NULL, 'Stefanco Veaceslav', '2222222222', '', true, false, 'Stefanco', 'Panie Stefanco', NULL);
INSERT INTO public.client VALUES (36, 34, 'a5500550020@gmail.com', NULL, 'Andrii Tavarian', '73060917218', '', true, false, 'Andrii', 'Panie Andrii', NULL);
INSERT INTO public.client VALUES (43, 41, 'wojciech@syjud.pl', NULL, 'Inteco Wojciech Syjud', '6121757509', '', true, true, 'Wojtek', 'Panie Wojciechu', NULL);
INSERT INTO public.client VALUES (37, 35, 'BIURO.ARDA24@GMAIL.COM', NULL, 'ARDA24 Sp. z o o', '8943242123', '', true, true, 'Arek', 'Panie Arkadiuszu', NULL);
INSERT INTO public.client VALUES (17, 17, 'ogorekpatryk08@gmail.com', NULL, 'O&O sp. z o.o.', '8982302059', '', true, true, 'Aleksander', 'Panie Aleksandrze', NULL);
INSERT INTO public.client VALUES (44, 42, 'mateuszkolacz.energy@gmail.com', NULL, 'HOME DETAL MATEUSZ KOŁACZ', '8971668348', '', true, true, 'Mateusz', 'Panie Mateuszu', NULL);
INSERT INTO public.client VALUES (11, 11, 'sabinajanowicz@gmail.com', NULL, 'Sabina Janowicz', '8941660885', '', true, true, 'Krzysztof', 'Pani Sabino', NULL);
INSERT INTO public.client VALUES (45, 43, 'rkrysiak@o2.pl', NULL, 'Radosław Krysiak', '75012213113', '', true, false, 'Radosław', 'Panie Radosławie', NULL);
INSERT INTO public.client VALUES (5, 5, 'info.armata@o2.pl', NULL, 'F.H.U. ARMATA LUKASZ NOWAK', '8942952408', '', true, false, 'Lukasz', 'Panie Łukaszu', NULL);
INSERT INTO public.client VALUES (46, 44, 'ik8@o2.pl', NULL, 'Paweł Włoszczyk', '73082308535', '', true, false, 'Paweł', 'Panie Pawle', NULL);
INSERT INTO public.client VALUES (41, 39, 'albertpiatek1@gmail.com', NULL, 'Imani Trade sp. zoo', '8971897787', '0000929814', true, true, 'Albert', 'Panie Albercie', NULL);
INSERT INTO public.client VALUES (47, 45, 'pivoteka.wroclaw@gmail.com', NULL, 'Aurora sp. z o.o.', '8992768322', '', true, true, 'Konstanty', 'Panie Konstantin', NULL);
INSERT INTO public.client VALUES (48, 46, 'michalmarek@gmail.com', NULL, 'Michał', '94062510957', '', true, false, 'Michał', 'Panie Michale', NULL);
INSERT INTO public.client VALUES (40, 38, 'aleksy.adamczyk1996@gmail.com', NULL, 'Aleksy Adamczyk', '96072911951', '', false, true, 'Aleksy', 'Panie Aleksy', NULL);
INSERT INTO public.client VALUES (2, 2, 'kornaspiotr27@gmail.com', NULL, 'FIRMA USŁUGOWA PIOTR KORNAŚ', '8992552674', '', false, true, 'Piotr', 'Panie Piotrze', NULL);
INSERT INTO public.client VALUES (7, 7, 'kashichidoshi.log@gmail.com', NULL, 'KASHICHIDOSHI sp zoo', '5223208007', '', false, false, 'Artur', 'Panie Arturze', NULL);
INSERT INTO public.client VALUES (6, 6, 'wimaxspzoo@gmail.com', NULL, 'WIMAX sp zoo', '8992890129', '', true, true, 'Maxym', 'Panie Maksymie', NULL);
INSERT INTO public.client VALUES (50, 48, 'm.kruszel@oponly.pl', NULL, 'KRUSZWIL MAREK KRUSZEL', '9552512205', '', true, true, 'Marek', 'Panie Marku', NULL);
INSERT INTO public.client VALUES (53, 51, 'ekspansja@meadway.pl', NULL, 'MEADWAY sp. z oo', '7171840779', '', false, true, 'Marcin Stanek', 'Panie Marcinie', NULL);
INSERT INTO public.client VALUES (54, 52, 'czekajlo.krzysztof@gmail.com', NULL, 'MERANTI KRZYSZTOF CZEKAJŁO', '8981863301', '', true, true, 'Krzysztof', 'Panie Krzysztofie', NULL);
INSERT INTO public.client VALUES (55, 53, 'aswierzbicki@wp.pl', NULL, '"AS" P.H.U. s.c. ANDRZEJ WIERZBICKI', '8821822960', '', true, true, 'Andrzej', 'Panie Andrzeju', NULL);
INSERT INTO public.client VALUES (56, 54, 'biuro.ukr.group@gmail.com', NULL, 'UKR GROUP sp. zoo', '8992969285', '', true, true, 'Maksym', 'Panie Maksymie', NULL);
INSERT INTO public.client VALUES (57, 55, 'biuro@sigvin.pl', NULL, 'SIGVIN sp. zoo', '8971829260', '', true, true, 'Andriy', 'Panie Andriy', NULL);
INSERT INTO public.client VALUES (58, 56, 'floris.ogrody@gmail.com', NULL, 'FLORIS OGRODY sp. zoo', '8971909734', '', true, true, 'Adam', 'Panie Adamie', NULL);
INSERT INTO public.client VALUES (59, 57, 'doandronik@gmail.com', NULL, 'Andro - Bud Oleg Andronik', '5423338200', '', true, true, 'Oleg', 'Panie Oleg', NULL);
INSERT INTO public.client VALUES (51, 49, 'edyta@halycz.pl', NULL, 'PATRYCJA SZEWCZYK', '6912343715', '', false, true, 'Andy', 'Pani Patrycjo', NULL);


--
-- TOC entry 3414 (class 0 OID 16401)
-- Dependencies: 221
-- Data for Name: dump_plac; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.dump_plac VALUES ('-- PostgreSQL database dump');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('-- Dumped from database version 15.1');
INSERT INTO public.dump_plac VALUES ('-- Dumped by pg_dump version 15.1');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('-- Started on 2024-07-01 14:33:21');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SET statement_timeout = 0;');
INSERT INTO public.dump_plac VALUES ('SET lock_timeout = 0;');
INSERT INTO public.dump_plac VALUES ('SET idle_in_transaction_session_timeout = 0;');
INSERT INTO public.dump_plac VALUES ('SET client_encoding = ''LATIN2'';');
INSERT INTO public.dump_plac VALUES ('SET standard_conforming_strings = on;');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.set_config(''search_path''');
INSERT INTO public.dump_plac VALUES ('SET check_function_bodies = false;');
INSERT INTO public.dump_plac VALUES ('SET xmloption = content;');
INSERT INTO public.dump_plac VALUES ('SET client_min_messages = warning;');
INSERT INTO public.dump_plac VALUES ('SET row_security = off;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 4 (class 2615 OID 2200)');
INSERT INTO public.dump_plac VALUES ('-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SCHEMA public;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SCHEMA public OWNER TO pg_database_owner;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3385 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 4');
INSERT INTO public.dump_plac VALUES ('-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COMMENT ON SCHEMA public IS ''standard public schema'';');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SET default_tablespace = '''';');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SET default_table_access_method = heap;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 225 (class 1259 OID 58258)');
INSERT INTO public.dump_plac VALUES ('-- Name: address; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.address (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    client_id bigint');
INSERT INTO public.dump_plac VALUES ('    street character varying(255)');
INSERT INTO public.dump_plac VALUES ('    home character varying(255)');
INSERT INTO public.dump_plac VALUES ('    local character varying(255)');
INSERT INTO public.dump_plac VALUES ('    city character varying(255)');
INSERT INTO public.dump_plac VALUES ('    post character varying(255)');
INSERT INTO public.dump_plac VALUES ('    country character varying(255)');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.address OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 224 (class 1259 OID 58257)');
INSERT INTO public.dump_plac VALUES ('-- Name: address_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.address_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.address_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3386 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 224');
INSERT INTO public.dump_plac VALUES ('-- Name: address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.address_id_seq OWNED BY public.address.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 217 (class 1259 OID 58222)');
INSERT INTO public.dump_plac VALUES ('-- Name: client; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.client (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    address_id bigint');
INSERT INTO public.dump_plac VALUES ('    email character varying(255)');
INSERT INTO public.dump_plac VALUES ('    phone character varying(255)');
INSERT INTO public.dump_plac VALUES ('    name character varying(255)');
INSERT INTO public.dump_plac VALUES ('    nip character varying(255)');
INSERT INTO public.dump_plac VALUES ('    krs character varying(255)');
INSERT INTO public.dump_plac VALUES ('    active boolean NOT NULL');
INSERT INTO public.dump_plac VALUES ('    need_invoice boolean NOT NULL');
INSERT INTO public.dump_plac VALUES ('    person_to_contact character varying(255)');
INSERT INTO public.dump_plac VALUES ('    salutation character varying(255)');
INSERT INTO public.dump_plac VALUES ('    products numeric[]');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.client OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 216 (class 1259 OID 58221)');
INSERT INTO public.dump_plac VALUES ('-- Name: client_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.client_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.client_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3387 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 216');
INSERT INTO public.dump_plac VALUES ('-- Name: client_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.client_id_seq OWNED BY public.client.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 219 (class 1259 OID 58233)');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.invoice (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    date date');
INSERT INTO public.dump_plac VALUES ('    main_account smallint');
INSERT INTO public.dump_plac VALUES ('    number character varying(255)');
INSERT INTO public.dump_plac VALUES ('    price numeric(38');
INSERT INTO public.dump_plac VALUES ('    price_with_tax numeric(38');
INSERT INTO public.dump_plac VALUES ('    tax numeric(38');
INSERT INTO public.dump_plac VALUES ('    tax_account smallint');
INSERT INTO public.dump_plac VALUES ('    client_id bigint');
INSERT INTO public.dump_plac VALUES ('    products_invoice numeric[]');
INSERT INTO public.dump_plac VALUES ('    send_date date');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.invoice OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 218 (class 1259 OID 58232)');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.invoice_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.invoice_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3388 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 218');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.invoice_id_seq OWNED BY public.invoice.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 221 (class 1259 OID 58242)');
INSERT INTO public.dump_plac VALUES ('-- Name: product; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.product (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    price numeric(38');
INSERT INTO public.dump_plac VALUES ('    price_with_vat numeric(38');
INSERT INTO public.dump_plac VALUES ('    product_enum smallint');
INSERT INTO public.dump_plac VALUES ('    quantity numeric(38');
INSERT INTO public.dump_plac VALUES ('    unit_price numeric(38');
INSERT INTO public.dump_plac VALUES ('    vat_amount numeric(38');
INSERT INTO public.dump_plac VALUES ('    vat_rate integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    client_id bigint');
INSERT INTO public.dump_plac VALUES ('    products_description numeric[]');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.product OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 220 (class 1259 OID 58241)');
INSERT INTO public.dump_plac VALUES ('-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.product_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.product_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3389 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 220');
INSERT INTO public.dump_plac VALUES ('-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.product_id_seq OWNED BY public.product.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 223 (class 1259 OID 58251)');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.product_invoice_dto (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    price numeric(38');
INSERT INTO public.dump_plac VALUES ('    price_with_vat numeric(38');
INSERT INTO public.dump_plac VALUES ('    product_enum smallint');
INSERT INTO public.dump_plac VALUES ('    quantity numeric(38');
INSERT INTO public.dump_plac VALUES ('    unit_price numeric(38');
INSERT INTO public.dump_plac VALUES ('    vat_amount numeric(38');
INSERT INTO public.dump_plac VALUES ('    vat_rate integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    client_id bigint');
INSERT INTO public.dump_plac VALUES ('    invoice_id bigint');
INSERT INTO public.dump_plac VALUES ('    send_date date');
INSERT INTO public.dump_plac VALUES ('    ');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.product_invoice_dto OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 222 (class 1259 OID 58250)');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.product_invoice_dto_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.product_invoice_dto_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3390 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 222');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.product_invoice_dto_id_seq OWNED BY public.product_invoice_dto.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 227 (class 1259 OID 58350)');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.productdescription (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    product_id bigint');
INSERT INTO public.dump_plac VALUES ('    client_id bigint');
INSERT INTO public.dump_plac VALUES ('    number character varying(255)');
INSERT INTO public.dump_plac VALUES ('    location character varying(255)');
INSERT INTO public.dump_plac VALUES ('    description character varying(255)');
INSERT INTO public.dump_plac VALUES ('    acquisition date');
INSERT INTO public.dump_plac VALUES ('    maintencedone date[]');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.productdescription OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 226 (class 1259 OID 58349)');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('8	8	Zwycieska	12B	7	Wroc�aw	53-033	PL');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.productdescription_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.productdescription_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3391 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 226');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.productdescription_id_seq OWNED BY public.productdescription.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 215 (class 1259 OID 57998)');
INSERT INTO public.dump_plac VALUES ('-- Name: user_login; Type: TABLE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE TABLE public.user_login (');
INSERT INTO public.dump_plac VALUES ('    id integer NOT NULL');
INSERT INTO public.dump_plac VALUES ('    username character varying(255)');
INSERT INTO public.dump_plac VALUES ('    password character varying(255)');
INSERT INTO public.dump_plac VALUES ('    role character varying(255)');
INSERT INTO public.dump_plac VALUES ('    account_expired boolean');
INSERT INTO public.dump_plac VALUES ('    account_locked boolean');
INSERT INTO public.dump_plac VALUES ('    credentials_expired boolean');
INSERT INTO public.dump_plac VALUES ('    enabled boolean');
INSERT INTO public.dump_plac VALUES ('    company_nip character varying(255)');
INSERT INTO public.dump_plac VALUES ('    company_id integer');
INSERT INTO public.dump_plac VALUES ('    first_name character varying(255)');
INSERT INTO public.dump_plac VALUES ('    last_name character varying(255)');
INSERT INTO public.dump_plac VALUES ('    email character varying(255)');
INSERT INTO public.dump_plac VALUES ('    tel character varying(255)');
INSERT INTO public.dump_plac VALUES (');');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.user_login OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 214 (class 1259 OID 57997)');
INSERT INTO public.dump_plac VALUES ('-- Name: user_login_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('CREATE SEQUENCE public.user_login_id_seq');
INSERT INTO public.dump_plac VALUES ('    AS integer');
INSERT INTO public.dump_plac VALUES ('    START WITH 1');
INSERT INTO public.dump_plac VALUES ('    INCREMENT BY 1');
INSERT INTO public.dump_plac VALUES ('    NO MINVALUE');
INSERT INTO public.dump_plac VALUES ('    NO MAXVALUE');
INSERT INTO public.dump_plac VALUES ('    CACHE 1;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE public.user_login_id_seq OWNER TO postgres;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3392 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 214');
INSERT INTO public.dump_plac VALUES ('-- Name: user_login_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER SEQUENCE public.user_login_id_seq OWNED BY public.user_login.id;');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3208 (class 2604 OID 58261)');
INSERT INTO public.dump_plac VALUES ('-- Name: address id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.address ALTER COLUMN id SET DEFAULT nextval(''public.address_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3204 (class 2604 OID 58225)');
INSERT INTO public.dump_plac VALUES ('-- Name: client id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.client ALTER COLUMN id SET DEFAULT nextval(''public.client_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3205 (class 2604 OID 58236)');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.invoice ALTER COLUMN id SET DEFAULT nextval(''public.invoice_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3206 (class 2604 OID 58245)');
INSERT INTO public.dump_plac VALUES ('-- Name: product id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.product ALTER COLUMN id SET DEFAULT nextval(''public.product_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3207 (class 2604 OID 58254)');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.product_invoice_dto ALTER COLUMN id SET DEFAULT nextval(''public.product_invoice_dto_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3209 (class 2604 OID 58353)');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.productdescription ALTER COLUMN id SET DEFAULT nextval(''public.productdescription_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3203 (class 2604 OID 58001)');
INSERT INTO public.dump_plac VALUES ('-- Name: user_login id; Type: DEFAULT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.user_login ALTER COLUMN id SET DEFAULT nextval(''public.user_login_id_seq''::regclass);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3377 (class 0 OID 58258)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 225');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: address; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.address (id');
INSERT INTO public.dump_plac VALUES ('7	7	Szczesliwa	26	N	Warszawa	02-454	PL');
INSERT INTO public.dump_plac VALUES ('10	10	1 MAJA	30A	N	Opole	45-355	PL');
INSERT INTO public.dump_plac VALUES ('2	2	Z�otostocka	27	9	Wroc�aw	50-511	PL');
INSERT INTO public.dump_plac VALUES ('1	1	Kasztanowa	18-20	115	Wroc�aw		PL');
INSERT INTO public.dump_plac VALUES ('3	3	Borowska	266	4	Wroc�aw	50-558	PL');
INSERT INTO public.dump_plac VALUES ('9	9	Rogowska	127	N	Wroc�aw	54-440	PL');
INSERT INTO public.dump_plac VALUES ('13	13	Graniczna	133	N	Wroc�aw	54-530	PL');
INSERT INTO public.dump_plac VALUES ('16	16	Wolbromska	18	1B	Wroc�aw	53-148	PL');
INSERT INTO public.dump_plac VALUES ('12	12	Grabiszy�ska	279	14	Wroc�aw	53-234	PL');
INSERT INTO public.dump_plac VALUES ('14	14	Tadeusza Ko�ciuszki	86	22	Wroc�aw	50-441	PL');
INSERT INTO public.dump_plac VALUES ('4	4	Jerzmanowska	127A	N	Wroc�aw	54-530	PL');
INSERT INTO public.dump_plac VALUES ('5	5	marsz. J�zefa Pi�sudskiego	74	320	Wroc�aw	50-020	PL');
INSERT INTO public.dump_plac VALUES ('11	11	Legnicka	52A	N	Wroc�aw	54-204	PL');
INSERT INTO public.dump_plac VALUES ('6	6	marsz. J�zefa Pi�sudskiego	266	4	Wroc�aw	50-020	PL');
INSERT INTO public.dump_plac VALUES ('17	17	J�zefa Rostafi�skiego	5	2	Wroc�aw	50-247	PL');
INSERT INTO public.dump_plac VALUES ('18	18	Przebendowskiego	21	34	Puck	84-100	PL');
INSERT INTO public.dump_plac VALUES ('15	15	Wolbromska	18	1B	Wroc�aw	53-148	PL');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3369 (class 0 OID 58222)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 217');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: client; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.client (id');
INSERT INTO public.dump_plac VALUES ('1	1	biuro@cedru.pl		CEDRU sp z o o	8971780140	N	t	t	Artur	Pani Magdo	N');
INSERT INTO public.dump_plac VALUES ('4	4	aceco@gazeta.pl	phone	ACECO Wojciech Szewczyk 	5651203445	N	t	t	Wojciech	Panie Wojtku	N');
INSERT INTO public.dump_plac VALUES ('6	6	wimaxspzoo@gmail.com	phone	WIMAX sp zoo	8992890129	N	t	t	Maxym	Panie Makasymie	N');
INSERT INTO public.dump_plac VALUES ('9	9	a.sikora@wpbsikora.com	phone	WPB Sikora	8943156493	N	t	t	Siergiej	N	N');
INSERT INTO public.dump_plac VALUES ('10	10	nazaruktaras89@gmail.com	phone	PHEONIX CAPITAL GROUP sp. zoo	7543342582	N	t	t	Takes	N	N');
INSERT INTO public.dump_plac VALUES ('11	11	em.wroclaw@gmail.com	phone	EM S.C. JANUSZ JANOWICZ');
INSERT INTO public.dump_plac VALUES ('15	15	elnalaz@gmail.com	phone	MARCIN NALAZEK	6121800458	N	t	t	Marcin	Panie Marcinie	N');
INSERT INTO public.dump_plac VALUES ('16	16	ORESTZHYLA@WP.PL	phone	Wromar	8943191058	N	t	f	OREST ZHYLA	Panie Orest	N');
INSERT INTO public.dump_plac VALUES ('12	12	krzychuszkarlat@wp.pl	N	SZKAR�AT BUDOWNICTWO sp Z O.O.	8851638830		t	t	Krzysztof	Panie Krzysztofie	N');
INSERT INTO public.dump_plac VALUES ('2	2	kornaspiotr27@gmail.com	N	FIRMA US�UGOWA PIOTR KORNA�	8992552674		t	t	Piotr	Panie Piotrze	N');
INSERT INTO public.dump_plac VALUES ('14	14	jagodka-jagodka@o2.pl	N	MI�-TRANS MATEUSZ MI�KIEWICZ	8992767015		t	t	Mateusz	Panie Z�oty	N');
INSERT INTO public.dump_plac VALUES ('3	3	sylwia.czemarnik@gmail.com	phone	STJ Wroc�aw	8982257152	N	t	t	Sylwia	Pani Sylwio	N');
INSERT INTO public.dump_plac VALUES ('8	8	utmlogistik@gmail.com	phone	APPA Joanna Karlinska	7431145511	N	t	t	m��	Pani Joanno	N');
INSERT INTO public.dump_plac VALUES ('7	7	kashichidoshi.log@gmail.com	phone	KASHICHIDOSHI sp zoo	5223208007	N	t	f	Artur	Panie Arturze	N');
INSERT INTO public.dump_plac VALUES ('5	5	info.armata@o2.pl	phone	F.H.U. ARMATA �UKASZ NOWAK	8942952408	N	t	f	�ukasz	Panie �ukaszu	N');
INSERT INTO public.dump_plac VALUES ('18	18	patrykbochen91@gmail.com	600 645 238	SCAFTECH PATRYK BOCHEN	5871666696	N	t	t	Patryk	Panie Patryku	{}');
INSERT INTO public.dump_plac VALUES ('17	17	roboty.ogorek@poczta.onet.pl	503 722 128	O&O sp. z o.o.	8982302059	N	t	t	Aleksander	Panie Aleksandrze	{}');
INSERT INTO public.dump_plac VALUES ('13	13	piotrzuko@wp.pl	phone	ZUKO Piotr Koba	8941081549	N	f	t	Piotr	Panie Piotrze	N');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3371 (class 0 OID 58233)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 219');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: invoice; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.invoice (id');
INSERT INTO public.dump_plac VALUES ('1	2024-06-03	0	1/6/2024	1392.50	1712.77	320.27	1	1	{2} NULL');
INSERT INTO public.dump_plac VALUES ('2	2024-06-03	0	2/6/2024	350.00	430.50	80.50	1	4	{1} NULL');
INSERT INTO public.dump_plac VALUES ('3	2024-06-03	0	3/6/2024	400.00	492.00	92.00	1	6	{3} NULL');
INSERT INTO public.dump_plac VALUES ('5	2024-06-03	0	5/6/2024	2221.80	2732.81	511.01	1	13	{6} NULL');
INSERT INTO public.dump_plac VALUES ('7	2024-06-03	0	7/6/2024	390.81	480.69	89.88	1	12	{8');
INSERT INTO public.dump_plac VALUES ('8	2024-06-03	0	9/6/2024	2645.00	3253.35	608.35	1	2	{10} NULL');
INSERT INTO public.dump_plac VALUES ('9	2024-06-03	0	4/6/2024	2688.00	3306.24	618.24	1	11	{4} NULL');
INSERT INTO public.dump_plac VALUES ('11	2024-06-03	0	11/6/2024	1000.00	1230.00	230.00	1	3	{11} NULL');
INSERT INTO public.dump_plac VALUES ('10	2024-06-03	0	10/6/2024	1151.30	1416.09	264.79	1	14	{12} NULL');
INSERT INTO public.dump_plac VALUES ('13	2024-06-03	0	13/6/2024	350.00	430.50	80.50	1	9	{14} NULL');
INSERT INTO public.dump_plac VALUES ('4	2024-06-03	0	8/6/2024	600.00	738.00	138.00	1	10	{5} NULL');
INSERT INTO public.dump_plac VALUES ('14	2024-06-03	0	12/6/2024	350.00	430.50	80.50	1	8	{13} NULL');
INSERT INTO public.dump_plac VALUES ('15	2024-06-06	0	14/6/2024	350.00	430.50	80.50	1	18	{15} NULL');
INSERT INTO public.dump_plac VALUES ('18	2024-06-07	0	15/6/2024	284.55	349.99	65.44	1	15	{19} NULL');
INSERT INTO public.dump_plac VALUES ('16	2024-06-07	0	6/6/2024	850.00	1045.50	195.50	1	17	{16');
INSERT INTO public.dump_plac VALUES ('24	2024-07-01	0	4/7/2024	350.00	430.50	80.50	1	9	{20} NULL');
INSERT INTO public.dump_plac VALUES ('25	2024-07-01	0	7/7/2024	284.55	349.99	65.44	1	15	{24} NULL');
INSERT INTO public.dump_plac VALUES ('21	2024-07-01	0	5/7/2024	600.00	738.00	138.00	1	10	{25} NULL');
INSERT INTO public.dump_plac VALUES ('23	2024-07-01	0	6/7/2024	2688.00	3306.24	618.24	1	11	{27} NULL');
INSERT INTO public.dump_plac VALUES ('20	2024-07-01	0	2/7/2024	350.00	430.50	80.50	1	4	{23} NULL');
INSERT INTO public.dump_plac VALUES ('19	2024-07-01	0	1/7/2024	1392.50	1712.77	320.27	1	1	{21} NULL');
INSERT INTO public.dump_plac VALUES ('22	2024-07-01	0	3/7/2024	400.00	492.00	92.00	1	6	{22} NULL');
INSERT INTO public.dump_plac VALUES ('31	2024-07-01	0	13/7/2024	350.00	430.50	80.50	1	18	{28} NULL');
INSERT INTO public.dump_plac VALUES ('30	2024-07-01	0	12/7/2024	350.00	430.50	80.50	1	8	{30} NULL');
INSERT INTO public.dump_plac VALUES ('26	2024-07-01	0	8/7/2024	390.81	480.69	89.88	1	12	{26');
INSERT INTO public.dump_plac VALUES ('29	2024-07-01	0	11/7/2024	1000.00	1230.00	230.00	1	3	{32} NULL');
INSERT INTO public.dump_plac VALUES ('27	2024-07-01	0	9/7/2024	2645.00	3253.35	608.35	1	2	{29} NULL');
INSERT INTO public.dump_plac VALUES ('28	2024-07-01	0	10/7/2024	1151.30	1416.09	264.79	1	14	{33} NULL');
INSERT INTO public.dump_plac VALUES ('32	2024-07-01	0	14/7/2024	850.00	1045.50	195.50	1	17	{34');
INSERT INTO public.dump_plac VALUES ('33	2024-07-01	0	1/7/2024	0.00	0.00	0.00	1	28	{} NULL');
INSERT INTO public.dump_plac VALUES ('34	2024-07-01	0	1/7/2024	0.00	0.00	0.00	1	28	{} NULL');
INSERT INTO public.dump_plac VALUES ('35	2024-07-01	0	1/7/2024	0.00	0.00	0.00	1	28	{} NULL');
INSERT INTO public.dump_plac VALUES ('36	2024-07-01	0	1/7/2024	0.00	0.00	0.00	1	28	{} NULL');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3373 (class 0 OID 58242)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 221');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.product (id');
INSERT INTO public.dump_plac VALUES ('1	1392.50	1712.77	0	250.00	5.57	320.27	23	1	N');
INSERT INTO public.dump_plac VALUES ('2	2645.00	3253.35	0	500.00	5.29	608.35	23	2	N');
INSERT INTO public.dump_plac VALUES ('3	1000.00	1230.00	6	2.00	500.00	230.00	23	3	N');
INSERT INTO public.dump_plac VALUES ('4	350.00	430.50	5	1.00	350.00	80.50	23	4	N');
INSERT INTO public.dump_plac VALUES ('5	284.55	350.00	5	1.00	350.00	65.44	23	5	N');
INSERT INTO public.dump_plac VALUES ('6	400.00	492.00	0	80.00	5.00	92.00	23	6	N');
INSERT INTO public.dump_plac VALUES ('7	350.00	430.50	5	1.00	350.00	80.50	23	7	N');
INSERT INTO public.dump_plac VALUES ('8	350.00	430.50	5	1.00	350.00	80.50	23	8	N');
INSERT INTO public.dump_plac VALUES ('9	350.00	430.50	5	1.00	350.00	80.50	23	9	N');
INSERT INTO public.dump_plac VALUES ('10	600.00	738.00	6	1.00	600.00	138.00	23	10	N');
INSERT INTO public.dump_plac VALUES ('11	2688.00	3306.24	0	700.00	3.84	618.24	23	11	N');
INSERT INTO public.dump_plac VALUES ('12	252.87	311.03	5	1.00	252.87	58.16	23	12	N');
INSERT INTO public.dump_plac VALUES ('13	137.94	169.66	1	1.00	137.94	31.72	23	12	N');
INSERT INTO public.dump_plac VALUES ('14	2221.80	2732.81	0	420.00	5.29	511.01	23	13	N');
INSERT INTO public.dump_plac VALUES ('15	1151.30	1416.10	0	290.00	3.97	264.79	23	14	N');
INSERT INTO public.dump_plac VALUES ('17	350.00	430.50	5	1.00	350.00	80.50	23	16	N');
INSERT INTO public.dump_plac VALUES ('18	350.00	430.50	0	70.00	5.00	80.50	23	18	{}');
INSERT INTO public.dump_plac VALUES ('19	350.00	430.50	5	1.00	350.00	80.50	23	17	{}');
INSERT INTO public.dump_plac VALUES ('20	500.00	615.00	0	100.00	5.00	115.00	23	17	{}');
INSERT INTO public.dump_plac VALUES ('16	284.55	349.99	5	1.00	284.55	65.44	23	15	N');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3375 (class 0 OID 58251)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 223');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: product_invoice_dto; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.product_invoice_dto (id');
INSERT INTO public.dump_plac VALUES ('2	1392.50	1712.77	0	250.00	5.57	320.27	23	1	1 NULL');
INSERT INTO public.dump_plac VALUES ('5	600.00	738.00	6	1.00	600.00	138.00	23	10	4 NULL');
INSERT INTO public.dump_plac VALUES ('11	1000.00	1230.00	6	2.00	500.00	230.00	23	3	11 NULL');
INSERT INTO public.dump_plac VALUES ('3	400.00	492.00	0	80.00	5.00	92.00	23	6	3 NULL');
INSERT INTO public.dump_plac VALUES ('14	350.00	430.50	5	1.00	350.00	80.50	23	9	13 NULL');
INSERT INTO public.dump_plac VALUES ('6	2221.80	2732.81	0	420.00	5.29	511.01	23	13	5 NULL');
INSERT INTO public.dump_plac VALUES ('4	2688.00	3306.24	0	700.00	3.84	618.24	23	11	9 NULL');
INSERT INTO public.dump_plac VALUES ('10	2645.00	3253.35	0	500.00	5.29	608.35	23	2	8 NULL');
INSERT INTO public.dump_plac VALUES ('7	350.00	430.50	5	1.00	350.00	80.50	23	15	6 NULL');
INSERT INTO public.dump_plac VALUES ('8	252.87	311.03	5	1.00	252.87	58.16	23	12	7 NULL');
INSERT INTO public.dump_plac VALUES ('9	137.94	169.66	1	1.00	137.94	31.72	23	12	7 NULL');
INSERT INTO public.dump_plac VALUES ('12	1151.30	1416.10	0	290.00	3.97	264.79	23	14	10 NULL');
INSERT INTO public.dump_plac VALUES ('13	350.00	430.50	5	1.00	350.00	80.50	23	8	14 NULL');
INSERT INTO public.dump_plac VALUES ('15	350.00	430.50	0	70.00	5.00	80.50	23	18	15 NULL');
INSERT INTO public.dump_plac VALUES ('20	350.00	430.50	5	1.00	350.00	80.50	23	9	24 NULL');
INSERT INTO public.dump_plac VALUES ('30	350.00	430.50	5	1.00	350.00	80.50	23	8	30 NULL');
INSERT INTO public.dump_plac VALUES ('16	350.00	430.50	5	1.00	350.00	80.50	23	17	16 NULL');
INSERT INTO public.dump_plac VALUES ('22	400.00	492.00	0	80.00	5.00	92.00	23	6	22 NULL');
INSERT INTO public.dump_plac VALUES ('29	2645.00	3253.35	0	500.00	5.29	608.35	23	2	27 NULL');
INSERT INTO public.dump_plac VALUES ('17	500.00	615.00	0	100.00	5.00	115.00	23	17	16 NULL');
INSERT INTO public.dump_plac VALUES ('21	1392.50	1712.77	0	250.00	5.57	320.27	23	1	19 NULL');
INSERT INTO public.dump_plac VALUES ('33	1151.30	1416.10	0	290.00	3.97	264.79	23	14	28 NULL');
INSERT INTO public.dump_plac VALUES ('18	284.55	349.99	5	1.00	284.55	65.44	23	15	17 NULL');
INSERT INTO public.dump_plac VALUES ('19	284.55	349.99	5	1.00	284.55	65.44	23	15	18 NULL');
INSERT INTO public.dump_plac VALUES ('23	350.00	430.50	5	1.00	350.00	80.50	23	4	20 NULL');
INSERT INTO public.dump_plac VALUES ('31	137.94	169.66	1	1.00	137.94	31.72	23	12	26 NULL');
INSERT INTO public.dump_plac VALUES ('24	284.55	349.99	5	1.00	284.55	65.44	23	15	25 NULL');
INSERT INTO public.dump_plac VALUES ('25	600.00	738.00	6	1.00	600.00	138.00	23	10	21 NULL');
INSERT INTO public.dump_plac VALUES ('32	1000.00	1230.00	6	2.00	500.00	230.00	23	3	29 NULL');
INSERT INTO public.dump_plac VALUES ('26	252.87	311.03	5	1.00	252.87	58.16	23	12	26 NULL');
INSERT INTO public.dump_plac VALUES ('27	2688.00	3306.24	0	700.00	3.84	618.24	23	11	23 NULL');
INSERT INTO public.dump_plac VALUES ('28	350.00	430.50	0	70.00	5.00	80.50	23	18	31 NULL');
INSERT INTO public.dump_plac VALUES ('34	350.00	430.50	5	1.00	350.00	80.50	23	17	32 NULL');
INSERT INTO public.dump_plac VALUES ('35	500.00	615.00	0	100.00	5.00	115.00	23	17	32 NULL');
INSERT INTO public.dump_plac VALUES ('1	350.00	430.50	5	1.00	350.00	80.50	23	4	2 NULL');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3379 (class 0 OID 58350)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 227');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: productdescription; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.productdescription (id');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3367 (class 0 OID 57998)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 215');
INSERT INTO public.dump_plac VALUES ('-- Data for Name: user_login; Type: TABLE DATA; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('COPY public.user_login (id');
INSERT INTO public.dump_plac VALUES ('1	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('2	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('3	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('4	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('5	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('6	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('7	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('8	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('9	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('10	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('11	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('12	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('13	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('14	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('15	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('16	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('17	user1	user1	USER	f	f	f	t		N	testNameUSER	N	N	N');
INSERT INTO public.dump_plac VALUES ('18	admin1	admin1	ADMIN	f	f	f	t	8942957044	N	testNameADMIN	N	N	N');
INSERT INTO public.dump_plac VALUES ('.');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3393 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 224');
INSERT INTO public.dump_plac VALUES ('-- Name: address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.address_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3394 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 216');
INSERT INTO public.dump_plac VALUES ('-- Name: client_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.client_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3395 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 218');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.invoice_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3396 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 220');
INSERT INTO public.dump_plac VALUES ('-- Name: product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.product_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3397 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 222');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.product_invoice_dto_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3398 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 226');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.productdescription_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3399 (class 0 OID 0)');
INSERT INTO public.dump_plac VALUES ('-- Dependencies: 214');
INSERT INTO public.dump_plac VALUES ('-- Name: user_login_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('SELECT pg_catalog.setval(''public.user_login_id_seq''');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3221 (class 2606 OID 58265)');
INSERT INTO public.dump_plac VALUES ('-- Name: address address_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.address');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT address_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3211 (class 2606 OID 58231)');
INSERT INTO public.dump_plac VALUES ('-- Name: client client_nip_key; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.client');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT client_nip_key UNIQUE (nip);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3213 (class 2606 OID 58229)');
INSERT INTO public.dump_plac VALUES ('-- Name: client client_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.client');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT client_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3215 (class 2606 OID 58240)');
INSERT INTO public.dump_plac VALUES ('-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.invoice');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3219 (class 2606 OID 58256)');
INSERT INTO public.dump_plac VALUES ('-- Name: product_invoice_dto product_invoice_dto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.product_invoice_dto');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT product_invoice_dto_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3217 (class 2606 OID 58249)');
INSERT INTO public.dump_plac VALUES ('-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.product');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT product_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- TOC entry 3223 (class 2606 OID 58357)');
INSERT INTO public.dump_plac VALUES ('-- Name: productdescription productdescription_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('ALTER TABLE ONLY public.productdescription');
INSERT INTO public.dump_plac VALUES ('    ADD CONSTRAINT productdescription_pkey PRIMARY KEY (id);');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('-- Completed on 2024-07-01 14:33:22');
INSERT INTO public.dump_plac VALUES ('');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('-- PostgreSQL database dump complete');
INSERT INTO public.dump_plac VALUES ('--');
INSERT INTO public.dump_plac VALUES ('');


--
-- TOC entry 3415 (class 0 OID 16404)
-- Dependencies: 222
-- Data for Name: invoice; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.invoice VALUES (5, '2024-08-01', 0, '5/8/2024', 350.00, 430.50, 80.50, 1, 4, '{5}', '2024-08-01');
INSERT INTO public.invoice VALUES (3, '2024-08-01', 0, '2/8/2024', 1392.50, 1712.77, 320.27, 1, 1, '{3}', '2024-08-01');
INSERT INTO public.invoice VALUES (6, '2024-08-01', 0, '6/8/2024', 350.00, 430.50, 80.50, 1, 8, '{6}', '2024-08-01');
INSERT INTO public.invoice VALUES (77, '2024-11-08', 0, '22/11/2024', 200.00, 246.00, 46.00, 1, 47, '{87}', NULL);
INSERT INTO public.invoice VALUES (37, '2024-10-01', 0, '3/10/2024', 350.00, 430.50, 80.50, 1, 4, '{44}', '2024-10-01');
INSERT INTO public.invoice VALUES (52, '2024-10-03', 0, '18/10/2024', 400.00, 492.00, 92.00, 1, 40, '{59}', '2024-10-03');
INSERT INTO public.invoice VALUES (54, '2024-10-14', 0, '19/10/2024', 266.00, 327.18, 61.18, 1, 42, '{61}', NULL);
INSERT INTO public.invoice VALUES (55, '2024-10-15', 0, '1/10/2024', 266.00, 327.18, 61.18, 1, 43, '{62}', NULL);
INSERT INTO public.invoice VALUES (35, '2024-10-01', 0, '21/10/2024', 1000.00, 1230.00, 230.00, 1, 3, '{41}', '2024-10-01');
INSERT INTO public.invoice VALUES (56, '2024-11-02', 0, '1/11/2024', 1392.50, 1712.77, 320.27, 1, 1, '{63}', '2024-11-02');
INSERT INTO public.invoice VALUES (64, '2024-11-02', 0, '9/11/2024', 1000.00, 1230.00, 230.00, 1, 22, '{71}', '2024-11-02');
INSERT INTO public.invoice VALUES (73, '2024-11-02', 0, '18/11/2024', 203.20, 249.93, 46.73, 1, 44, '{82}', '2024-11-02');
INSERT INTO public.invoice VALUES (74, '2024-11-02', 0, '19/11/2024', 2688.00, 3306.24, 618.24, 1, 11, '{83}', '2024-11-02');
INSERT INTO public.invoice VALUES (124, '2025-02-01', 0, '5/2/2025', 400.00, 492.00, 92.00, 1, 6, '{141}', '2025-02-01');
INSERT INTO public.invoice VALUES (81, '2024-12-02', 0, '5/12/2024', 400.00, 492.00, 92.00, 1, 6, '{92}', '2024-12-02');
INSERT INTO public.invoice VALUES (79, '2024-12-02', 0, '2/12/2024', 1392.50, 1712.77, 320.27, 1, 1, '{90}', '2024-12-02');
INSERT INTO public.invoice VALUES (96, '2024-12-02', 0, '19/12/2024', 2688.00, 3306.24, 618.24, 1, 11, '{109}', '2024-12-02');
INSERT INTO public.invoice VALUES (80, '2024-12-02', 0, '4/12/2024', 350.00, 430.50, 80.50, 1, 4, '{91}', '2024-12-02');
INSERT INTO public.invoice VALUES (140, '2025-02-01', 0, '18/2/2025', 2688.00, 3306.24, 618.24, 1, 11, '{156}', '2025-02-01');
INSERT INTO public.invoice VALUES (100, '2024-12-12', 0, '22/12/2024', 400.00, 492.00, 92.00, 1, 47, '{}', '2024-12-14');
INSERT INTO public.invoice VALUES (167, '2025-04-01', 0, '1/4/2025', 400.00, 492.00, 92.00, 1, 6, '{190}', '2025-04-01');
INSERT INTO public.invoice VALUES (101, '2025-01-01', 0, '1/1/2025', 350.00, 430.50, 80.50, 1, 8, '{114}', '2025-01-01');
INSERT INTO public.invoice VALUES (110, '2025-01-01', 0, '12/1/2025', 400.00, 492.00, 92.00, 1, 42, '{121}', '2025-01-01');
INSERT INTO public.invoice VALUES (109, '2025-01-01', 0, '11/1/2025', 350.00, 430.50, 80.50, 1, 18, '{120}', '2025-01-01');
INSERT INTO public.invoice VALUES (102, '2025-01-01', 0, '2/1/2025', 1392.50, 1712.77, 320.27, 1, 1, '{115}', '2025-01-01');
INSERT INTO public.invoice VALUES (108, '2025-01-01', 0, '10/1/2025', 350.00, 430.50, 80.50, 1, 49, '{128}', '2025-01-01');
INSERT INTO public.invoice VALUES (111, '2025-01-01', 0, '13/1/2025', 284.50, 349.93, 65.43, 1, 15, '{126}', '2025-01-01');
INSERT INTO public.invoice VALUES (143, '2025-03-05', 0, '1/3/2025', 1000.00, 1230.00, 230.00, 1, 3, '{162}', '2025-03-05');
INSERT INTO public.invoice VALUES (154, '2025-03-05', 0, '12/3/2025', 400.00, 492.00, 92.00, 1, 42, '{173}', '2025-03-05');
INSERT INTO public.invoice VALUES (165, '2025-03-05', 0, '23/3/2025', 400.00, 492.00, 92.00, 1, 51, '{186}', '2025-03-05');
INSERT INTO public.invoice VALUES (185, '2025-04-01', 0, '11/4/2025', 350.00, 430.50, 80.50, 1, 18, '{195}', '2025-04-01');
INSERT INTO public.invoice VALUES (191, '2025-05-02', 0, '1/5/2025', 1392.50, 1712.77, 320.27, 1, 1, '{216}', NULL);
INSERT INTO public.invoice VALUES (9, '2024-08-01', 0, '11/8/2024', 1151.30, 1416.09, 264.79, 1, 14, '{7}', '2024-08-01');
INSERT INTO public.invoice VALUES (4, '2024-08-01', 0, '4/8/2024', 1000.00, 1230.00, 230.00, 1, 3, '{4}', '2024-08-01');
INSERT INTO public.invoice VALUES (11, '2024-08-01', 0, '13/8/2024', 850.00, 1045.50, 195.50, 1, 17, '{15,18}', '2024-08-01');
INSERT INTO public.invoice VALUES (78, '2024-12-02', 0, '1/12/2024', 600.00, 738.00, 138.00, 1, 10, '{88}', '2024-12-02');
INSERT INTO public.invoice VALUES (50, '2024-10-01', 0, '9/10/2024', 1000.00, 1230.00, 230.00, 1, 22, '{49}', '2024-10-01');
INSERT INTO public.invoice VALUES (51, '2024-10-01', 0, '17/10/2024', 2688.00, 3306.24, 618.24, 1, 11, '{57}', '2024-10-01');
INSERT INTO public.invoice VALUES (36, '2024-10-01', 0, '2/10/2024', 1392.50, 1712.77, 320.27, 1, 1, '{43}', '2024-10-01');
INSERT INTO public.invoice VALUES (53, '2024-10-03', 0, '20/10/2024', 400.00, 492.00, 92.00, 1, 41, '{60}', NULL);
INSERT INTO public.invoice VALUES (59, '2024-11-02', 0, '4/11/2024', 400.00, 492.00, 92.00, 1, 6, '{65}', '2024-11-02');
INSERT INTO public.invoice VALUES (57, '2024-11-02', 0, '2/11/2024', 1000.00, 1230.00, 230.00, 1, 3, '{64}', '2024-11-02');
INSERT INTO public.invoice VALUES (66, '2024-11-02', 0, '11/11/2024', 400.00, 492.00, 92.00, 1, 42, '{73}', '2024-11-02');
INSERT INTO public.invoice VALUES (72, '2024-11-02', 0, '17/11/2024', 1275.00, 1568.25, 293.25, 1, 17, '{80,81}', '2024-11-02');
INSERT INTO public.invoice VALUES (69, '2024-11-02', 0, '15/11/2024', 400.00, 492.00, 92.00, 1, 43, '{77}', '2024-11-02');
INSERT INTO public.invoice VALUES (104, '2025-01-01', 0, '4/1/2025', 350.00, 430.50, 80.50, 1, 4, '{116}', '2025-01-01');
INSERT INTO public.invoice VALUES (105, '2025-01-01', 0, '5/1/2025', 400.00, 492.00, 92.00, 1, 6, '{117}', '2025-01-01');
INSERT INTO public.invoice VALUES (103, '2025-01-01', 0, '3/1/2025', 1000.00, 1230.00, 230.00, 1, 3, '{113}', '2025-01-01');
INSERT INTO public.invoice VALUES (117, '2025-01-01', 0, '9/1/2025', 1000.00, 1230.00, 230.00, 1, 22, '{132}', '2025-01-01');
INSERT INTO public.invoice VALUES (129, '2025-02-01', 0, '6/2/2025', 350.00, 430.50, 80.50, 1, 8, '{142}', '2025-02-01');
INSERT INTO public.invoice VALUES (132, '2025-02-01', 0, '11/2/2025', 400.00, 492.00, 92.00, 1, 42, '{145}', '2025-02-01');
INSERT INTO public.invoice VALUES (134, '2025-02-01', 0, '13/2/2025', 1151.30, 1416.09, 264.79, 1, 14, '{146}', '2025-02-01');
INSERT INTO public.invoice VALUES (133, '2025-02-01', 0, '12/2/2025', 284.50, 349.93, 65.43, 1, 15, '{153}', '2025-02-01');
INSERT INTO public.invoice VALUES (168, '2025-04-01', 0, '2/4/2025', 1392.50, 1712.77, 320.27, 1, 1, '{191}', '2025-04-01');
INSERT INTO public.invoice VALUES (170, '2025-04-01', 0, '4/4/2025', 350.00, 430.50, 80.50, 1, 4, '{189}', '2025-04-01');
INSERT INTO public.invoice VALUES (186, '2025-04-01', 0, '9/4/2025', 1000.00, 1230.00, 230.00, 1, 22, '{194}', '2025-04-01');
INSERT INTO public.invoice VALUES (173, '2025-04-01', 0, '7/4/2025', 390.81, 480.69, 89.88, 1, 12, '{200,201}', '2025-04-01');
INSERT INTO public.invoice VALUES (144, '2025-03-05', 0, '2/3/2025', 1392.50, 1712.77, 320.27, 1, 1, '{161}', '2025-03-05');
INSERT INTO public.invoice VALUES (149, '2025-03-05', 0, '6/3/2025', 600.00, 738.00, 138.00, 1, 10, '{165}', '2025-03-05');
INSERT INTO public.invoice VALUES (150, '2025-03-05', 0, '8/3/2025', 1000.00, 1230.00, 230.00, 1, 38, '{169}', '2025-03-05');
INSERT INTO public.invoice VALUES (152, '2025-03-05', 0, '9/3/2025', 1000.00, 1230.00, 230.00, 1, 22, '{170}', '2025-03-05');
INSERT INTO public.invoice VALUES (158, '2025-03-05', 0, '16/3/2025', 400.00, 492.00, 92.00, 1, 43, '{177}', '2025-03-05');
INSERT INTO public.invoice VALUES (159, '2025-03-05', 0, '17/3/2025', 440.00, 541.20, 101.20, 1, 37, '{178,179}', '2025-03-05');
INSERT INTO public.invoice VALUES (163, '2025-03-05', 0, '21/3/2025', 400.00, 492.00, 92.00, 1, 41, '{184}', '2025-03-05');
INSERT INTO public.invoice VALUES (164, '2025-03-05', 0, '22/3/2025', 800.00, 984.00, 184.00, 1, 50, '{185}', '2025-03-05');
INSERT INTO public.invoice VALUES (192, '2025-05-02', 0, '2/5/2025', 1000.00, 1230.00, 230.00, 1, 3, '{217}', NULL);
INSERT INTO public.invoice VALUES (199, '2025-05-02', 0, '9/5/2025', 350.00, 430.50, 80.50, 1, 49, '{221}', NULL);
INSERT INTO public.invoice VALUES (198, '2025-05-02', 0, '8/5/2025', 1000.00, 1230.00, 230.00, 1, 22, '{224}', NULL);
INSERT INTO public.invoice VALUES (211, '2025-05-02', 0, '21/5/2025', 1275.00, 1568.25, 293.25, 1, 17, '{236,237}', NULL);
INSERT INTO public.invoice VALUES (212, '2025-05-02', 0, '22/5/2025', 400.00, 492.00, 92.00, 1, 51, '{238}', NULL);
INSERT INTO public.invoice VALUES (213, '2025-05-02', 0, '23/5/2025', 800.00, 984.00, 184.00, 1, 50, '{239}', NULL);
INSERT INTO public.invoice VALUES (214, '2025-05-02', 0, '24/5/2025', 400.00, 492.00, 92.00, 1, 47, '{241}', NULL);
INSERT INTO public.invoice VALUES (58, '2024-11-02', 0, '3/11/2024', 350.00, 430.50, 80.50, 1, 4, '{86}', '2024-11-02');
INSERT INTO public.invoice VALUES (2, '2024-08-01', 0, '3/8/2024', 2645.00, 3253.35, 608.35, 1, 2, '{2}', '2024-08-01');
INSERT INTO public.invoice VALUES (7, '2024-08-01', 0, '7/8/2024', 600.00, 738.00, 138.00, 1, 10, '{11}', '2024-08-01');
INSERT INTO public.invoice VALUES (8, '2024-08-01', 0, '8/8/2024', 2688.00, 3306.24, 618.24, 1, 11, '{13}', '2024-08-01');
INSERT INTO public.invoice VALUES (39, '2024-10-01', 0, '5/10/2024', 350.00, 430.50, 80.50, 1, 8, '{42}', '2024-10-01');
INSERT INTO public.invoice VALUES (38, '2024-10-01', 0, '4/10/2024', 400.00, 492.00, 92.00, 1, 6, '{45}', '2024-10-01');
INSERT INTO public.invoice VALUES (60, '2024-11-02', 0, '5/11/2024', 350.00, 430.50, 80.50, 1, 8, '{69}', '2024-11-02');
INSERT INTO public.invoice VALUES (68, '2024-11-02', 0, '13/11/2024', 1151.30, 1416.09, 264.79, 1, 14, '{75}', '2024-11-02');
INSERT INTO public.invoice VALUES (70, '2024-11-02', 0, '14/11/2024', 2645.00, 3253.35, 608.35, 1, 2, '{76}', '2024-11-02');
INSERT INTO public.invoice VALUES (82, '2024-12-02', 0, '6/12/2024', 350.00, 430.50, 80.50, 1, 8, '{93}', '2024-12-02');
INSERT INTO public.invoice VALUES (91, '2024-12-02', 0, '17/12/2024', 1275.00, 1568.25, 293.25, 1, 17, '{105,106}', '2024-12-02');
INSERT INTO public.invoice VALUES (92, '2024-12-02', 0, '18/12/2024', 203.20, 249.93, 46.73, 1, 44, '{108}', '2024-12-02');
INSERT INTO public.invoice VALUES (99, '2024-12-02', 0, '20/12/2024', 400.00, 492.00, 92.00, 1, 40, '{111}', '2024-12-02');
INSERT INTO public.invoice VALUES (122, '2025-02-01', 0, '3/2/2025', 1000.00, 1230.00, 230.00, 1, 3, '{137}', '2025-02-01');
INSERT INTO public.invoice VALUES (130, '2025-02-01', 0, '9/2/2025', 1000.00, 1230.00, 230.00, 1, 22, '{150}', '2025-02-01');
INSERT INTO public.invoice VALUES (139, '2025-02-01', 0, '19/2/2025', 400.00, 492.00, 92.00, 1, 41, '{157}', '2025-02-01');
INSERT INTO public.invoice VALUES (113, '2025-01-01', 0, '16/1/2025', 440.00, 541.20, 101.20, 1, 37, '{123,125}', '2025-01-01');
INSERT INTO public.invoice VALUES (106, '2025-01-01', 0, '6/1/2025', 600.00, 738.00, 138.00, 1, 10, '{127}', '2025-01-01');
INSERT INTO public.invoice VALUES (114, '2025-01-01', 0, '17/1/2025', 1275.00, 1568.25, 293.25, 1, 17, '{129,130}', '2025-01-01');
INSERT INTO public.invoice VALUES (115, '2025-01-01', 0, '18/1/2025', 203.20, 249.93, 46.73, 1, 44, '{131}', '2025-01-01');
INSERT INTO public.invoice VALUES (112, '2025-01-01', 0, '15/1/2025', 400.00, 492.00, 92.00, 1, 43, '{133}', '2025-01-01');
INSERT INTO public.invoice VALUES (171, '2025-04-01', 0, '5/4/2025', 350.00, 430.50, 80.50, 1, 8, '{188}', '2025-04-01');
INSERT INTO public.invoice VALUES (145, '2025-03-05', 0, '3/3/2025', 350.00, 430.50, 80.50, 1, 4, '{163}', '2025-03-05');
INSERT INTO public.invoice VALUES (153, '2025-03-05', 0, '11/3/2025', 350.00, 430.50, 80.50, 1, 18, '{172}', '2025-03-05');
INSERT INTO public.invoice VALUES (156, '2025-03-05', 0, '14/3/2025', 1151.30, 1416.09, 264.79, 1, 14, '{176}', '2025-03-05');
INSERT INTO public.invoice VALUES (160, '2025-03-05', 0, '18/3/2025', 1275.00, 1568.25, 293.25, 1, 17, '{180,181}', '2025-03-05');
INSERT INTO public.invoice VALUES (169, '2025-04-01', 0, '3/4/2025', 1000.00, 1230.00, 230.00, 1, 3, '{197}', '2025-04-01');
INSERT INTO public.invoice VALUES (188, '2025-04-01', 0, '10/4/2025', 350.00, 430.50, 80.50, 1, 49, '{199}', '2025-04-01');
INSERT INTO public.invoice VALUES (193, '2025-05-02', 0, '4/5/2025', 350.00, 430.50, 80.50, 1, 8, '{219}', NULL);
INSERT INTO public.invoice VALUES (202, '2025-05-02', 0, '13/5/2025', 284.50, 349.93, 65.43, 1, 15, '{227}', NULL);
INSERT INTO public.invoice VALUES (206, '2025-05-02', 0, '16/5/2025', 400.00, 492.00, 92.00, 1, 43, '{231}', NULL);
INSERT INTO public.invoice VALUES (208, '2025-05-02', 0, '18/5/2025', 203.20, 249.93, 46.73, 1, 44, '{232}', NULL);
INSERT INTO public.invoice VALUES (207, '2025-05-02', 0, '17/5/2025', 440.00, 541.20, 101.20, 1, 37, '{230,233}', NULL);
INSERT INTO public.invoice VALUES (210, '2025-05-02', 0, '20/5/2025', 400.00, 492.00, 92.00, 1, 41, '{235}', NULL);
INSERT INTO public.invoice VALUES (12, '2024-08-01', 0, '14/8/2024', 350.00, 430.50, 80.50, 1, 18, '{10}', '2024-08-01');
INSERT INTO public.invoice VALUES (41, '2024-10-01', 0, '8/10/2024', 1000.00, 1230.00, 230.00, 1, 38, '{48}', '2024-10-01');
INSERT INTO public.invoice VALUES (40, '2024-10-01', 0, '7/10/2024', 390.81, 480.69, 89.88, 1, 12, '{46,47}', '2024-10-01');
INSERT INTO public.invoice VALUES (61, '2024-11-02', 0, '7/11/2024', 390.81, 480.69, 89.88, 1, 12, '{68,67}', '2024-11-02');
INSERT INTO public.invoice VALUES (63, '2024-11-02', 0, '8/11/2024', 1000.00, 1230.00, 230.00, 1, 38, '{70}', '2024-11-02');
INSERT INTO public.invoice VALUES (65, '2024-11-02', 0, '10/11/2024', 350.00, 430.50, 80.50, 1, 18, '{72}', '2024-11-02');
INSERT INTO public.invoice VALUES (71, '2024-11-02', 0, '16/11/2024', 440.00, 541.20, 101.20, 1, 37, '{79,78}', '2024-11-02');
INSERT INTO public.invoice VALUES (76, '2024-11-02', 0, '21/11/2024', 400.00, 492.00, 92.00, 1, 41, '{85}', '2024-11-02');
INSERT INTO public.invoice VALUES (128, '2025-02-01', 0, '8/2/2025', 1000.00, 1230.00, 230.00, 1, 38, '{144}', '2025-02-01');
INSERT INTO public.invoice VALUES (93, '2024-12-02', 0, '15/12/2024', 400.00, 492.00, 92.00, 1, 43, '{98}', '2024-12-02');
INSERT INTO public.invoice VALUES (86, '2024-12-02', 0, '10/12/2024', 350.00, 430.50, 80.50, 1, 18, '{96}', '2024-12-02');
INSERT INTO public.invoice VALUES (87, '2024-12-02', 0, '11/12/2024', 400.00, 492.00, 92.00, 1, 42, '{97}', '2024-12-02');
INSERT INTO public.invoice VALUES (85, '2024-12-02', 0, '9/12/2024', 1000.00, 1230.00, 230.00, 1, 22, '{95}', '2024-12-02');
INSERT INTO public.invoice VALUES (83, '2024-12-02', 0, '7/12/2024', 390.81, 480.69, 89.88, 1, 12, '{94,103}', '2024-12-02');
INSERT INTO public.invoice VALUES (135, '2025-02-01', 0, '14/2/2025', 400.00, 492.00, 92.00, 1, 43, '{147}', '2025-02-01');
INSERT INTO public.invoice VALUES (107, '2025-01-01', 0, '7/1/2025', 390.81, 480.69, 89.88, 1, 12, '{118,119}', '2025-01-01');
INSERT INTO public.invoice VALUES (146, '2025-03-05', 0, '4/3/2025', 400.00, 492.00, 92.00, 1, 6, '{166}', '2025-03-05');
INSERT INTO public.invoice VALUES (148, '2025-03-05', 0, '5/3/2025', 350.00, 430.50, 80.50, 1, 8, '{164}', '2025-03-05');
INSERT INTO public.invoice VALUES (176, '2025-04-01', 0, '13/4/2025', 284.50, 349.93, 65.43, 1, 15, '{198}', '2025-04-01');
INSERT INTO public.invoice VALUES (172, '2025-04-01', 0, '6/4/2025', 600.00, 738.00, 138.00, 1, 10, '{192}', '2025-04-01');
INSERT INTO public.invoice VALUES (182, '2025-04-01', 0, '20/4/2025', 2688.00, 3306.24, 618.24, 1, 11, '{208}', '2025-04-01');
INSERT INTO public.invoice VALUES (183, '2025-04-01', 0, '21/4/2025', 400.00, 492.00, 92.00, 1, 41, '{209}', '2025-04-01');
INSERT INTO public.invoice VALUES (177, '2025-04-01', 0, '14/4/2025', 1151.30, 1416.09, 264.79, 1, 14, '{212}', '2025-04-01');
INSERT INTO public.invoice VALUES (194, '2025-05-02', 0, '3/5/2025', 350.00, 430.50, 80.50, 1, 4, '{215}', NULL);
INSERT INTO public.invoice VALUES (201, '2025-05-02', 0, '10/5/2025', 400.00, 492.00, 92.00, 1, 6, '{222}', NULL);
INSERT INTO public.invoice VALUES (195, '2025-05-02', 0, '6/5/2025', 390.81, 480.69, 89.88, 1, 12, '{223,225}', NULL);
INSERT INTO public.invoice VALUES (13, '2024-08-01', 0, '15/8/2024', 1000.00, 1230.00, 230.00, 1, 22, '{9}', '2024-08-01');
INSERT INTO public.invoice VALUES (14, '2024-08-01', 0, '16/8/2024', 350.00, 430.50, 80.50, 1, 9, '{12}', '2024-08-01');
INSERT INTO public.invoice VALUES (42, '2024-10-01', 0, '10/10/2024', 0.00, 0.00, 0.00, 1, 5, '{}', '2024-10-01');
INSERT INTO public.invoice VALUES (49, '2024-10-01', 0, '16/10/2024', 1275.00, 1568.25, 293.25, 1, 17, '{55,56}', '2024-10-01');
INSERT INTO public.invoice VALUES (44, '2024-10-01', 0, '12/10/2024', 284.50, 349.93, 65.43, 1, 15, '{51}', '2024-10-01');
INSERT INTO public.invoice VALUES (43, '2024-10-01', 0, '11/10/2024', 350.00, 430.50, 80.50, 1, 18, '{50}', '2024-10-01');
INSERT INTO public.invoice VALUES (48, '2024-10-01', 0, '15/10/2024', 440.00, 541.20, 101.20, 1, 37, '{53,54}', '2024-10-01');
INSERT INTO public.invoice VALUES (47, '2024-10-01', 0, '14/10/2024', 2645.00, 3253.35, 608.35, 1, 2, '{52}', '2024-10-01');
INSERT INTO public.invoice VALUES (46, '2024-10-01', 0, '13/10/2024', 1151.30, 1416.09, 264.79, 1, 14, '{58}', '2024-10-01');
INSERT INTO public.invoice VALUES (174, '2025-04-01', 0, '8/4/2025', 1000.00, 1230.00, 230.00, 1, 38, '{193}', '2025-04-01');
INSERT INTO public.invoice VALUES (62, '2024-11-02', 0, '6/11/2024', 600.00, 738.00, 138.00, 1, 10, '{66}', '2024-11-02');
INSERT INTO public.invoice VALUES (67, '2024-11-02', 0, '12/11/2024', 284.50, 349.93, 65.43, 1, 15, '{74}', '2024-11-02');
INSERT INTO public.invoice VALUES (75, '2024-11-02', 0, '20/11/2024', 400.00, 492.00, 92.00, 1, 40, '{84}', '2024-11-02');
INSERT INTO public.invoice VALUES (88, '2024-12-02', 0, '12/12/2024', 284.50, 349.93, 65.43, 1, 15, '{107}', '2024-12-02');
INSERT INTO public.invoice VALUES (89, '2024-12-02', 0, '13/12/2024', 1151.30, 1416.09, 264.79, 1, 14, '{102}', '2024-12-02');
INSERT INTO public.invoice VALUES (123, '2025-02-01', 0, '4/2/2025', 350.00, 430.50, 80.50, 1, 4, '{139}', '2025-02-01');
INSERT INTO public.invoice VALUES (84, '2024-12-02', 0, '8/12/2024', 1000.00, 1230.00, 230.00, 1, 38, '{101}', '2024-12-02');
INSERT INTO public.invoice VALUES (95, '2024-12-02', 0, '16/12/2024', 440.00, 541.20, 101.20, 1, 37, '{99,100}', '2024-12-02');
INSERT INTO public.invoice VALUES (137, '2025-02-01', 0, '16/2/2025', 1275.00, 1568.25, 293.25, 1, 17, '{152,155}', '2025-02-01');
INSERT INTO public.invoice VALUES (116, '2025-01-01', 0, '19/1/2025', 2688.00, 3306.24, 618.24, 1, 11, '{134}', '2025-01-01');
INSERT INTO public.invoice VALUES (142, '2025-02-01', 0, '20/2/2025', 400.00, 492.00, 92.00, 1, 47, '{160}', '2025-02-01');
INSERT INTO public.invoice VALUES (196, '2025-05-02', 0, '5/5/2025', 600.00, 738.00, 138.00, 1, 10, '{218}', NULL);
INSERT INTO public.invoice VALUES (147, '2025-03-05', 0, '7/3/2025', 390.81, 480.69, 89.88, 1, 12, '{167,168}', '2025-03-05');
INSERT INTO public.invoice VALUES (151, '2025-03-05', 0, '10/3/2025', 350.00, 430.50, 80.50, 1, 49, '{171}', '2025-03-05');
INSERT INTO public.invoice VALUES (161, '2025-03-05', 0, '19/3/2025', 203.20, 249.93, 46.73, 1, 44, '{182}', '2025-03-05');
INSERT INTO public.invoice VALUES (15, '2024-08-01', 0, '10/8/2024', 2221.80, 2732.81, 511.01, 1, 13, '{16}', '2024-08-01');
INSERT INTO public.invoice VALUES (45, '2024-10-01', 0, '6/10/2024', 600.00, 738.00, 138.00, 1, 10, '{40}', '2024-10-01');
INSERT INTO public.invoice VALUES (97, '2024-12-02', 0, '21/12/2024', 400.00, 492.00, 92.00, 1, 41, '{112}', '2024-12-02');
INSERT INTO public.invoice VALUES (90, '2024-12-02', 0, '3/12/2024', 1000.00, 1230.00, 230.00, 1, 3, '{89}', '2024-12-02');
INSERT INTO public.invoice VALUES (125, '2025-02-01', 0, '2/2/2025', 1392.50, 1712.77, 320.27, 1, 1, '{140}', '2025-02-01');
INSERT INTO public.invoice VALUES (119, '2025-01-01', 0, '8/1/2025', 1000.00, 1230.00, 230.00, 1, 38, '{124}', '2025-01-01');
INSERT INTO public.invoice VALUES (155, '2025-03-05', 0, '13/3/2025', 284.50, 349.93, 65.43, 1, 15, '{174}', '2025-03-05');
INSERT INTO public.invoice VALUES (157, '2025-03-05', 0, '15/3/2025', 800.00, 984.00, 184.00, 1, 52, '{175}', '2025-03-05');
INSERT INTO public.invoice VALUES (162, '2025-03-05', 0, '20/3/2025', 2688.00, 3306.24, 618.24, 1, 11, '{183}', '2025-03-05');
INSERT INTO public.invoice VALUES (175, '2025-04-01', 0, '12/4/2025', 400.00, 492.00, 92.00, 1, 42, '{196}', '2025-04-01');
INSERT INTO public.invoice VALUES (178, '2025-04-01', 0, '15/4/2025', 800.00, 984.00, 184.00, 1, 52, '{202}', '2025-04-01');
INSERT INTO public.invoice VALUES (187, '2025-04-01', 0, '17/4/2025', 440.00, 541.20, 101.20, 1, 37, '{203,206}', '2025-04-01');
INSERT INTO public.invoice VALUES (197, '2025-05-02', 0, '7/5/2025', 1000.00, 1230.00, 230.00, 1, 38, '{220}', NULL);
INSERT INTO public.invoice VALUES (16, '2024-08-01', 0, '9/8/2024', 390.81, 480.69, 89.88, 1, 12, '{14,17}', '2024-08-01');
INSERT INTO public.invoice VALUES (94, '2024-12-02', 0, '14/12/2024', 2645.00, 3253.35, 608.35, 1, 2, '{104}', '2024-12-02');
INSERT INTO public.invoice VALUES (118, '2025-01-01', 0, '14/1/2025', 1151.30, 1416.09, 264.79, 1, 14, '{122}', '2025-01-01');
INSERT INTO public.invoice VALUES (126, '2025-02-01', 0, '1/2/2025', 600.00, 738.00, 138.00, 1, 10, '{138}', '2025-02-01');
INSERT INTO public.invoice VALUES (141, '2025-02-01', 0, '21/2/2025', 350.00, 430.50, 80.50, 1, 18, '{159}', '2025-02-01');
INSERT INTO public.invoice VALUES (166, '2025-03-05', 0, '24/3/2025', 400.00, 492.00, 92.00, 1, 47, '{187}', '2025-03-05');
INSERT INTO public.invoice VALUES (179, '2025-04-01', 0, '16/4/2025', 400.00, 492.00, 92.00, 1, 43, '{204}', '2025-04-01');
INSERT INTO public.invoice VALUES (180, '2025-04-01', 0, '18/4/2025', 1275.00, 1568.25, 293.25, 1, 17, '{205,207}', '2025-04-01');
INSERT INTO public.invoice VALUES (181, '2025-04-01', 0, '19/4/2025', 203.20, 249.93, 46.73, 1, 44, '{210}', '2025-04-01');
INSERT INTO public.invoice VALUES (200, '2025-05-02', 0, '11/5/2025', 350.00, 430.50, 80.50, 1, 18, '{226}', NULL);
INSERT INTO public.invoice VALUES (203, '2025-05-02', 0, '14/5/2025', 1151.30, 1416.09, 264.79, 1, 14, '{228}', NULL);
INSERT INTO public.invoice VALUES (33, '2024-09-02', 0, '6/9/2024', 600.00, 738.00, 138.00, 1, 10, '{39}', '2024-09-02');
INSERT INTO public.invoice VALUES (17, '2024-08-02', 0, '17/8/2024', 1800.00, 2214.00, 414.00, 1, 5, '{19}', NULL);
INSERT INTO public.invoice VALUES (18, '2024-09-02', 0, '1/9/2024', 1392.50, 1712.77, 320.27, 1, 1, '{21}', '2024-09-02');
INSERT INTO public.invoice VALUES (120, '2025-01-01', 0, '20/1/2025', 400.00, 492.00, 92.00, 1, 41, '{135}', '2025-01-01');
INSERT INTO public.invoice VALUES (121, '2025-01-01', 0, '21/1/2025', 400.00, 492.00, 92.00, 1, 47, '{136}', '2025-01-01');
INSERT INTO public.invoice VALUES (127, '2025-02-01', 0, '7/2/2025', 390.81, 480.69, 89.88, 1, 12, '{143,149}', '2025-02-01');
INSERT INTO public.invoice VALUES (138, '2025-02-01', 0, '17/2/2025', 203.20, 249.93, 46.73, 1, 44, '{158}', '2025-02-01');
INSERT INTO public.invoice VALUES (184, '2025-04-01', 0, '22/4/2025', 400.00, 492.00, 92.00, 1, 47, '{211}', '2025-04-01');
INSERT INTO public.invoice VALUES (189, '2025-04-01', 0, '23/4/2025', 400.00, 492.00, 92.00, 1, 51, '{213}', '2025-04-01');
INSERT INTO public.invoice VALUES (190, '2025-04-01', 0, '24/4/2025', 800.00, 984.00, 184.00, 1, 50, '{214}', '2025-04-01');
INSERT INTO public.invoice VALUES (204, '2025-05-02', 0, '12/5/2025', 400.00, 492.00, 92.00, 1, 42, '{240}', NULL);
INSERT INTO public.invoice VALUES (20, '2024-09-02', 0, '3/9/2024', 350.00, 430.50, 80.50, 1, 4, '{20}', '2024-09-02');
INSERT INTO public.invoice VALUES (19, '2024-09-02', 0, '2/9/2024', 1000.00, 1230.00, 230.00, 1, 3, '{22}', '2024-09-02');
INSERT INTO public.invoice VALUES (131, '2025-02-01', 0, '10/2/2025', 350.00, 430.50, 80.50, 1, 49, '{151}', '2025-02-01');
INSERT INTO public.invoice VALUES (136, '2025-02-01', 0, '15/2/2025', 440.00, 541.20, 101.20, 1, 37, '{148,154}', '2025-02-01');
INSERT INTO public.invoice VALUES (205, '2025-05-02', 0, '15/5/2025', 800.00, 984.00, 184.00, 1, 52, '{229}', NULL);
INSERT INTO public.invoice VALUES (209, '2025-05-02', 0, '19/5/2025', 2688.00, 3306.24, 618.24, 1, 11, '{234}', NULL);
INSERT INTO public.invoice VALUES (32, '2024-09-02', 0, '7/9/2024', 2688.00, 3306.24, 618.24, 1, 11, '{38}', '2024-09-02');
INSERT INTO public.invoice VALUES (22, '2024-09-02', 0, '5/9/2024', 350.00, 430.50, 80.50, 1, 8, '{23}', '2024-09-02');
INSERT INTO public.invoice VALUES (21, '2024-09-02', 0, '4/9/2024', 400.00, 492.00, 92.00, 1, 6, '{27}', '2024-09-02');
INSERT INTO public.invoice VALUES (23, '2024-09-02', 0, '8/9/2024', 390.81, 480.69, 89.88, 1, 12, '{24,25}', '2024-09-02');
INSERT INTO public.invoice VALUES (30, '2024-09-02', 0, '15/9/2024', 2645.00, 3253.35, 608.35, 1, 2, '{33}', '2024-09-02');
INSERT INTO public.invoice VALUES (29, '2024-09-02', 0, '14/9/2024', 1151.30, 1416.09, 264.79, 1, 14, '{32}', '2024-09-02');
INSERT INTO public.invoice VALUES (25, '2024-09-02', 0, '10/9/2024', 1000.00, 1230.00, 230.00, 1, 22, '{28}', '2024-09-02');
INSERT INTO public.invoice VALUES (24, '2024-09-02', 0, '9/9/2024', 500.00, 615.00, 115.00, 1, 38, '{26}', '2024-09-02');
INSERT INTO public.invoice VALUES (28, '2024-09-02', 0, '13/9/2024', 284.50, 349.93, 65.43, 1, 15, '{31}', '2024-09-02');
INSERT INTO public.invoice VALUES (26, '2024-09-02', 0, '11/9/2024', 250.00, 307.50, 57.50, 1, 5, '{29}', '2024-09-02');
INSERT INTO public.invoice VALUES (34, '2024-09-02', 0, '17/9/2024', 440.00, 541.20, 101.20, 1, 37, '{36,37}', '2024-09-02');
INSERT INTO public.invoice VALUES (27, '2024-09-02', 0, '12/9/2024', 350.00, 430.50, 80.50, 1, 18, '{30}', '2024-09-02');
INSERT INTO public.invoice VALUES (31, '2024-09-02', 0, '16/9/2024', 850.00, 1045.50, 195.50, 1, 17, '{34,35}', '2024-09-02');
INSERT INTO public.invoice VALUES (10, '2024-08-01', 0, '12/8/2024', 350.00, 430.50, 80.50, 1, 15, '{8}', '2024-08-01');
INSERT INTO public.invoice VALUES (1, '2024-08-01', 0, '1/8/2024', 400.00, 492.00, 92.00, 1, 6, '{1}', '2024-08-01');
INSERT INTO public.invoice VALUES (215, '2025-07-02', 0, '2/7/2025', 1392.50, 1712.77, 320.27, 1, 1, '{242}', NULL);
INSERT INTO public.invoice VALUES (216, '2025-07-02', 0, '3/7/2025', 350.00, 430.50, 80.50, 1, 4, '{244}', NULL);
INSERT INTO public.invoice VALUES (224, '2025-07-02', 0, '1/7/2025', 1000.00, 1230.00, 230.00, 1, 3, '{243}', NULL);
INSERT INTO public.invoice VALUES (217, '2025-07-02', 0, '4/7/2025', 600.00, 738.00, 138.00, 1, 10, '{245}', NULL);
INSERT INTO public.invoice VALUES (219, '2025-07-02', 0, '6/7/2025', 1000.00, 1230.00, 230.00, 1, 38, '{247}', NULL);
INSERT INTO public.invoice VALUES (220, '2025-07-02', 0, '7/7/2025', 1000.00, 1230.00, 230.00, 1, 22, '{248}', NULL);
INSERT INTO public.invoice VALUES (218, '2025-07-02', 0, '5/7/2025', 390.81, 480.69, 89.88, 1, 12, '{246,249}', NULL);
INSERT INTO public.invoice VALUES (221, '2025-07-02', 0, '8/7/2025', 350.00, 430.50, 80.50, 1, 49, '{250}', NULL);
INSERT INTO public.invoice VALUES (223, '2025-07-02', 0, '9/7/2025', 350.00, 430.50, 80.50, 1, 8, '{251}', NULL);
INSERT INTO public.invoice VALUES (222, '2025-07-02', 0, '10/7/2025', 350.00, 430.50, 80.50, 1, 18, '{252}', NULL);
INSERT INTO public.invoice VALUES (225, '2025-07-02', 0, '11/7/2025', 400.00, 492.00, 92.00, 1, 42, '{253}', NULL);
INSERT INTO public.invoice VALUES (227, '2025-07-02', 0, '14/7/2025', 800.00, 984.00, 184.00, 1, 52, '{254}', NULL);
INSERT INTO public.invoice VALUES (229, '2025-07-02', 0, '16/7/2025', 440.00, 541.20, 101.20, 1, 37, '{256,257}', NULL);
INSERT INTO public.invoice VALUES (226, '2025-07-02', 0, '12/7/2025', 284.50, 349.93, 65.43, 1, 15, '{258}', NULL);
INSERT INTO public.invoice VALUES (231, '2025-07-02', 0, '13/7/2025', 1151.30, 1416.09, 264.79, 1, 14, '{259}', NULL);
INSERT INTO public.invoice VALUES (228, '2025-07-02', 0, '15/7/2025', 400.00, 492.00, 92.00, 1, 43, '{255}', NULL);
INSERT INTO public.invoice VALUES (232, '2025-07-02', 0, '18/7/2025', 2688.00, 3306.24, 618.24, 1, 11, '{260}', NULL);
INSERT INTO public.invoice VALUES (233, '2025-07-02', 0, '19/7/2025', 203.20, 249.93, 46.73, 1, 44, '{262}', NULL);
INSERT INTO public.invoice VALUES (234, '2025-07-02', 0, '20/7/2025', 400.00, 492.00, 92.00, 1, 47, '{263}', NULL);
INSERT INTO public.invoice VALUES (230, '2025-07-02', 0, '17/7/2025', 1275.00, 1568.25, 293.25, 1, 17, '{261,264}', NULL);
INSERT INTO public.invoice VALUES (235, '2025-07-02', 0, '21/7/2025', 400.00, 492.00, 92.00, 1, 51, '{265}', NULL);
INSERT INTO public.invoice VALUES (236, '2025-07-02', 0, '22/7/2025', 400.00, 492.00, 92.00, 1, 41, '{266}', NULL);
INSERT INTO public.invoice VALUES (237, '2025-07-02', 0, '23/7/2025', 400.00, 492.00, 92.00, 1, 6, '{268}', NULL);
INSERT INTO public.invoice VALUES (240, '2025-07-02', 0, '26/7/2025', 800.00, 984.00, 184.00, 1, 50, '{269}', NULL);
INSERT INTO public.invoice VALUES (238, '2025-07-02', 0, '24/7/2025', 450.00, 553.50, 103.50, 1, 54, '{267}', NULL);
INSERT INTO public.invoice VALUES (244, '2025-07-02', 0, '27/7/2025', 750.00, 922.50, 172.50, 1, 56, '{270,273}', NULL);
INSERT INTO public.invoice VALUES (242, '2025-07-02', 0, '29/7/2025', 450.00, 553.50, 103.50, 1, 58, '{272}', NULL);
INSERT INTO public.invoice VALUES (239, '2025-07-02', 0, '25/7/2025', 750.00, 922.50, 172.50, 1, 55, '{275}', NULL);
INSERT INTO public.invoice VALUES (243, '2025-07-02', 0, '30/7/2025', 400.00, 492.00, 92.00, 1, 59, '{274}', NULL);
INSERT INTO public.invoice VALUES (241, '2025-07-02', 0, '28/7/2025', 750.00, 922.50, 172.50, 1, 57, '{271}', NULL);


--
-- TOC entry 3417 (class 0 OID 16410)
-- Dependencies: 224
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.product VALUES (1, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, NULL);
INSERT INTO public.product VALUES (2, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, NULL);
INSERT INTO public.product VALUES (3, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, NULL);
INSERT INTO public.product VALUES (4, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, NULL);
INSERT INTO public.product VALUES (6, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, NULL);
INSERT INTO public.product VALUES (8, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, NULL);
INSERT INTO public.product VALUES (9, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 9, NULL);
INSERT INTO public.product VALUES (10, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, NULL);
INSERT INTO public.product VALUES (11, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, NULL);
INSERT INTO public.product VALUES (12, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, NULL);
INSERT INTO public.product VALUES (13, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, NULL);
INSERT INTO public.product VALUES (14, 2221.80, 2732.81, 0, 420.00, 5.29, 511.01, 23, 13, NULL);
INSERT INTO public.product VALUES (15, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, NULL);
INSERT INTO public.product VALUES (17, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 16, NULL);
INSERT INTO public.product VALUES (18, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, NULL);
INSERT INTO public.product VALUES (20, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, NULL);
INSERT INTO public.product VALUES (22, 600.00, 738.00, 0, 1.00, 600.00, 138.00, 23, 7, NULL);
INSERT INTO public.product VALUES (24, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, NULL);
INSERT INTO public.product VALUES (26, 1.00, 1.23, 0, 1.00, 1.00, 0.23, 23, 7, NULL);
INSERT INTO public.product VALUES (28, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, NULL);
INSERT INTO public.product VALUES (33, 1050.00, 1291.50, 5, 3.00, 350.00, 241.50, 23, 24, NULL);
INSERT INTO public.product VALUES (34, 1200.00, 1476.00, 6, 2.00, 600.00, 276.00, 23, 24, NULL);
INSERT INTO public.product VALUES (36, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, NULL);
INSERT INTO public.product VALUES (37, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, NULL);
INSERT INTO public.product VALUES (39, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, NULL);
INSERT INTO public.product VALUES (40, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 39, NULL);
INSERT INTO public.product VALUES (41, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, NULL);
INSERT INTO public.product VALUES (43, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, NULL);
INSERT INTO public.product VALUES (46, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, NULL);
INSERT INTO public.product VALUES (48, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, NULL);
INSERT INTO public.product VALUES (50, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, NULL);
INSERT INTO public.product VALUES (51, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 45, NULL);
INSERT INTO public.product VALUES (52, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 46, NULL);
INSERT INTO public.product VALUES (54, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 48, NULL);
INSERT INTO public.product VALUES (55, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, NULL);
INSERT INTO public.product VALUES (56, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, NULL);
INSERT INTO public.product VALUES (57, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 50, NULL);
INSERT INTO public.product VALUES (59, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 52, NULL);
INSERT INTO public.product VALUES (60, 450.00, 553.50, 5, 1.00, 450.00, 103.50, 23, 53, NULL);
INSERT INTO public.product VALUES (61, 450.00, 553.50, 5, 1.00, 450.00, 103.50, 23, 54, NULL);
INSERT INTO public.product VALUES (62, 750.00, 922.50, 6, 1.00, 750.00, 172.50, 23, 55, NULL);
INSERT INTO public.product VALUES (63, 350.00, 430.50, 0, 7.00, 50.00, 80.50, 23, 56, NULL);
INSERT INTO public.product VALUES (64, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 56, NULL);
INSERT INTO public.product VALUES (65, 750.00, 922.50, 6, 750.00, 1.00, 172.50, 23, 57, NULL);
INSERT INTO public.product VALUES (66, 450.00, 553.50, 5, 1.00, 450.00, 103.50, 23, 58, NULL);
INSERT INTO public.product VALUES (67, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 59, NULL);


--
-- TOC entry 3419 (class 0 OID 16416)
-- Dependencies: 226
-- Data for Name: product_invoice_dto; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.product_invoice_dto VALUES (2, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, 2);
INSERT INTO public.product_invoice_dto VALUES (14, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 16);
INSERT INTO public.product_invoice_dto VALUES (59, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 40, 52);
INSERT INTO public.product_invoice_dto VALUES (61, 266.00, 327.18, 5, 1.00, 266.00, 61.18, 23, 42, 54);
INSERT INTO public.product_invoice_dto VALUES (62, 266.00, 327.18, 5, 1.00, 266.00, 61.18, 23, 43, 55);
INSERT INTO public.product_invoice_dto VALUES (63, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 56);
INSERT INTO public.product_invoice_dto VALUES (68, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 61);
INSERT INTO public.product_invoice_dto VALUES (69, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 60);
INSERT INTO public.product_invoice_dto VALUES (73, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 66);
INSERT INTO public.product_invoice_dto VALUES (76, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, 70);
INSERT INTO public.product_invoice_dto VALUES (95, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 85);
INSERT INTO public.product_invoice_dto VALUES (89, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 90);
INSERT INTO public.product_invoice_dto VALUES (110, 200.00, 246.00, 5, 1.00, 200.00, 46.00, 23, 47, 98);
INSERT INTO public.product_invoice_dto VALUES (112, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 97);
INSERT INTO public.product_invoice_dto VALUES (114, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 101);
INSERT INTO public.product_invoice_dto VALUES (132, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 117);
INSERT INTO public.product_invoice_dto VALUES (137, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 122);
INSERT INTO public.product_invoice_dto VALUES (153, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 133);
INSERT INTO public.product_invoice_dto VALUES (160, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 142);
INSERT INTO public.product_invoice_dto VALUES (161, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 144);
INSERT INTO public.product_invoice_dto VALUES (165, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 149);
INSERT INTO public.product_invoice_dto VALUES (164, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 148);
INSERT INTO public.product_invoice_dto VALUES (179, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 159);
INSERT INTO public.product_invoice_dto VALUES (181, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 160);
INSERT INTO public.product_invoice_dto VALUES (189, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 170);
INSERT INTO public.product_invoice_dto VALUES (215, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 194);
INSERT INTO public.product_invoice_dto VALUES (227, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 202);
INSERT INTO public.product_invoice_dto VALUES (238, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 51, 212);
INSERT INTO public.product_invoice_dto VALUES (236, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 211);
INSERT INTO public.product_invoice_dto VALUES (239, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 50, 213);
INSERT INTO public.product_invoice_dto VALUES (3, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 3);
INSERT INTO public.product_invoice_dto VALUES (11, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 7);
INSERT INTO public.product_invoice_dto VALUES (60, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 53);
INSERT INTO public.product_invoice_dto VALUES (64, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 57);
INSERT INTO public.product_invoice_dto VALUES (71, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 64);
INSERT INTO public.product_invoice_dto VALUES (90, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 79);
INSERT INTO public.product_invoice_dto VALUES (93, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 82);
INSERT INTO public.product_invoice_dto VALUES (88, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 78);
INSERT INTO public.product_invoice_dto VALUES (102, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 89);
INSERT INTO public.product_invoice_dto VALUES (103, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 83);
INSERT INTO public.product_invoice_dto VALUES (108, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 92);
INSERT INTO public.product_invoice_dto VALUES (111, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 40, 99);
INSERT INTO public.product_invoice_dto VALUES (115, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 102);
INSERT INTO public.product_invoice_dto VALUES (130, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 114);
INSERT INTO public.product_invoice_dto VALUES (139, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 123);
INSERT INTO public.product_invoice_dto VALUES (156, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 140);
INSERT INTO public.product_invoice_dto VALUES (162, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 143);
INSERT INTO public.product_invoice_dto VALUES (166, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 146);
INSERT INTO public.product_invoice_dto VALUES (167, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 147);
INSERT INTO public.product_invoice_dto VALUES (174, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 155);
INSERT INTO public.product_invoice_dto VALUES (176, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 156);
INSERT INTO public.product_invoice_dto VALUES (188, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 171);
INSERT INTO public.product_invoice_dto VALUES (216, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 191);
INSERT INTO public.product_invoice_dto VALUES (223, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 195);
INSERT INTO public.product_invoice_dto VALUES (5, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 5);
INSERT INTO public.product_invoice_dto VALUES (65, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 59);
INSERT INTO public.product_invoice_dto VALUES (91, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 80);
INSERT INTO public.product_invoice_dto VALUES (107, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 88);
INSERT INTO public.product_invoice_dto VALUES (117, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 105);
INSERT INTO public.product_invoice_dto VALUES (113, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 103);
INSERT INTO public.product_invoice_dto VALUES (127, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 106);
INSERT INTO public.product_invoice_dto VALUES (138, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 126);
INSERT INTO public.product_invoice_dto VALUES (149, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 127);
INSERT INTO public.product_invoice_dto VALUES (163, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 145);
INSERT INTO public.product_invoice_dto VALUES (170, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 152);
INSERT INTO public.product_invoice_dto VALUES (172, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 153);
INSERT INTO public.product_invoice_dto VALUES (175, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 52, 157);
INSERT INTO public.product_invoice_dto VALUES (187, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 166);
INSERT INTO public.product_invoice_dto VALUES (190, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 167);
INSERT INTO public.product_invoice_dto VALUES (217, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 192);
INSERT INTO public.product_invoice_dto VALUES (224, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 198);
INSERT INTO public.product_invoice_dto VALUES (232, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 208);
INSERT INTO public.product_invoice_dto VALUES (230, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 207);
INSERT INTO public.product_invoice_dto VALUES (241, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 214);
INSERT INTO public.product_invoice_dto VALUES (6, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 6);
INSERT INTO public.product_invoice_dto VALUES (13, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 8);
INSERT INTO public.product_invoice_dto VALUES (16, 2221.80, 2732.81, 0, 420.00, 5.29, 511.01, 23, 13, 15);
INSERT INTO public.product_invoice_dto VALUES (18, 500.00, 615.00, 1, 100.00, 5.00, 115.00, 23, 17, 11);
INSERT INTO public.product_invoice_dto VALUES (66, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 62);
INSERT INTO public.product_invoice_dto VALUES (70, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 63);
INSERT INTO public.product_invoice_dto VALUES (72, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 65);
INSERT INTO public.product_invoice_dto VALUES (81, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 72);
INSERT INTO public.product_invoice_dto VALUES (83, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 74);
INSERT INTO public.product_invoice_dto VALUES (85, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 76);
INSERT INTO public.product_invoice_dto VALUES (92, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 81);
INSERT INTO public.product_invoice_dto VALUES (101, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 84);
INSERT INTO public.product_invoice_dto VALUES (105, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 91);
INSERT INTO public.product_invoice_dto VALUES (116, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 104);
INSERT INTO public.product_invoice_dto VALUES (133, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 112);
INSERT INTO public.product_invoice_dto VALUES (140, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 125);
INSERT INTO public.product_invoice_dto VALUES (150, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 130);
INSERT INTO public.product_invoice_dto VALUES (168, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 147);
INSERT INTO public.product_invoice_dto VALUES (171, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 151);
INSERT INTO public.product_invoice_dto VALUES (173, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 154);
INSERT INTO public.product_invoice_dto VALUES (177, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 158);
INSERT INTO public.product_invoice_dto VALUES (180, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 160);
INSERT INTO public.product_invoice_dto VALUES (185, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 50, 164);
INSERT INTO public.product_invoice_dto VALUES (191, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 168);
INSERT INTO public.product_invoice_dto VALUES (205, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 180);
INSERT INTO public.product_invoice_dto VALUES (210, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 181);
INSERT INTO public.product_invoice_dto VALUES (201, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 173);
INSERT INTO public.product_invoice_dto VALUES (207, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 180);
INSERT INTO public.product_invoice_dto VALUES (218, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 196);
INSERT INTO public.product_invoice_dto VALUES (7, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 9);
INSERT INTO public.product_invoice_dto VALUES (67, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 61);
INSERT INTO public.product_invoice_dto VALUES (74, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 67);
INSERT INTO public.product_invoice_dto VALUES (75, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 68);
INSERT INTO public.product_invoice_dto VALUES (77, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 69);
INSERT INTO public.product_invoice_dto VALUES (79, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 71);
INSERT INTO public.product_invoice_dto VALUES (80, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 72);
INSERT INTO public.product_invoice_dto VALUES (82, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 73);
INSERT INTO public.product_invoice_dto VALUES (98, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 93);
INSERT INTO public.product_invoice_dto VALUES (94, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 83);
INSERT INTO public.product_invoice_dto VALUES (99, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 95);
INSERT INTO public.product_invoice_dto VALUES (100, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 95);
INSERT INTO public.product_invoice_dto VALUES (118, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 107);
INSERT INTO public.product_invoice_dto VALUES (120, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 109);
INSERT INTO public.product_invoice_dto VALUES (129, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 114);
INSERT INTO public.product_invoice_dto VALUES (134, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 116);
INSERT INTO public.product_invoice_dto VALUES (136, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 121);
INSERT INTO public.product_invoice_dto VALUES (141, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 124);
INSERT INTO public.product_invoice_dto VALUES (158, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 138);
INSERT INTO public.product_invoice_dto VALUES (155, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 137);
INSERT INTO public.product_invoice_dto VALUES (169, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 150);
INSERT INTO public.product_invoice_dto VALUES (186, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 51, 165);
INSERT INTO public.product_invoice_dto VALUES (192, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 172);
INSERT INTO public.product_invoice_dto VALUES (200, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 173);
INSERT INTO public.product_invoice_dto VALUES (219, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 193);
INSERT INTO public.product_invoice_dto VALUES (225, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 195);
INSERT INTO public.product_invoice_dto VALUES (229, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 52, 205);
INSERT INTO public.product_invoice_dto VALUES (235, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 210);
INSERT INTO public.product_invoice_dto VALUES (233, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 207);
INSERT INTO public.product_invoice_dto VALUES (237, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 211);
INSERT INTO public.product_invoice_dto VALUES (8, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 15, 10);
INSERT INTO public.product_invoice_dto VALUES (78, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 71);
INSERT INTO public.product_invoice_dto VALUES (84, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 40, 75);
INSERT INTO public.product_invoice_dto VALUES (96, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 86);
INSERT INTO public.product_invoice_dto VALUES (106, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 91);
INSERT INTO public.product_invoice_dto VALUES (119, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 107);
INSERT INTO public.product_invoice_dto VALUES (128, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 108);
INSERT INTO public.product_invoice_dto VALUES (131, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 115);
INSERT INTO public.product_invoice_dto VALUES (142, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 129);
INSERT INTO public.product_invoice_dto VALUES (151, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 131);
INSERT INTO public.product_invoice_dto VALUES (159, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 141);
INSERT INTO public.product_invoice_dto VALUES (178, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 159);
INSERT INTO public.product_invoice_dto VALUES (182, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 161);
INSERT INTO public.product_invoice_dto VALUES (183, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 162);
INSERT INTO public.product_invoice_dto VALUES (184, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 163);
INSERT INTO public.product_invoice_dto VALUES (193, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 174);
INSERT INTO public.product_invoice_dto VALUES (208, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 182);
INSERT INTO public.product_invoice_dto VALUES (206, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 187);
INSERT INTO public.product_invoice_dto VALUES (220, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 197);
INSERT INTO public.product_invoice_dto VALUES (228, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 203);
INSERT INTO public.product_invoice_dto VALUES (231, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 206);
INSERT INTO public.product_invoice_dto VALUES (234, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 209);
INSERT INTO public.product_invoice_dto VALUES (240, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 204);
INSERT INTO public.product_invoice_dto VALUES (10, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 12);
INSERT INTO public.product_invoice_dto VALUES (15, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 11);
INSERT INTO public.product_invoice_dto VALUES (17, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 16);
INSERT INTO public.product_invoice_dto VALUES (86, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 58);
INSERT INTO public.product_invoice_dto VALUES (97, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 87);
INSERT INTO public.product_invoice_dto VALUES (121, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 110);
INSERT INTO public.product_invoice_dto VALUES (126, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 111);
INSERT INTO public.product_invoice_dto VALUES (144, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 128);
INSERT INTO public.product_invoice_dto VALUES (143, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 127);
INSERT INTO public.product_invoice_dto VALUES (197, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 169);
INSERT INTO public.product_invoice_dto VALUES (194, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 186);
INSERT INTO public.product_invoice_dto VALUES (195, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 185);
INSERT INTO public.product_invoice_dto VALUES (202, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 52, 178);
INSERT INTO public.product_invoice_dto VALUES (204, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 179);
INSERT INTO public.product_invoice_dto VALUES (209, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 183);
INSERT INTO public.product_invoice_dto VALUES (221, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 199);
INSERT INTO public.product_invoice_dto VALUES (9, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 13);
INSERT INTO public.product_invoice_dto VALUES (12, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 9, 14);
INSERT INTO public.product_invoice_dto VALUES (87, 200.00, 246.00, 5, 1.00, 200.00, 46.00, 23, 47, 77);
INSERT INTO public.product_invoice_dto VALUES (104, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, 94);
INSERT INTO public.product_invoice_dto VALUES (109, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 96);
INSERT INTO public.product_invoice_dto VALUES (123, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 113);
INSERT INTO public.product_invoice_dto VALUES (122, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 118);
INSERT INTO public.product_invoice_dto VALUES (125, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 113);
INSERT INTO public.product_invoice_dto VALUES (145, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 132);
INSERT INTO public.product_invoice_dto VALUES (152, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 137);
INSERT INTO public.product_invoice_dto VALUES (196, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 175);
INSERT INTO public.product_invoice_dto VALUES (203, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 187);
INSERT INTO public.product_invoice_dto VALUES (211, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 184);
INSERT INTO public.product_invoice_dto VALUES (222, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 201);
INSERT INTO public.product_invoice_dto VALUES (226, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 200);
INSERT INTO public.product_invoice_dto VALUES (19, 1800.00, 2214.00, 0, 6.00, 300.00, 414.00, 23, 5, 17);
INSERT INTO public.product_invoice_dto VALUES (20, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 20);
INSERT INTO public.product_invoice_dto VALUES (38, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 32);
INSERT INTO public.product_invoice_dto VALUES (41, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 35);
INSERT INTO public.product_invoice_dto VALUES (124, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 119);
INSERT INTO public.product_invoice_dto VALUES (146, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 134);
INSERT INTO public.product_invoice_dto VALUES (157, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 139);
INSERT INTO public.product_invoice_dto VALUES (154, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 136);
INSERT INTO public.product_invoice_dto VALUES (198, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 176);
INSERT INTO public.product_invoice_dto VALUES (214, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 50, 190);
INSERT INTO public.product_invoice_dto VALUES (21, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 18);
INSERT INTO public.product_invoice_dto VALUES (42, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 39);
INSERT INTO public.product_invoice_dto VALUES (40, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 45);
INSERT INTO public.product_invoice_dto VALUES (53, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 48);
INSERT INTO public.product_invoice_dto VALUES (135, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 120);
INSERT INTO public.product_invoice_dto VALUES (147, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 135);
INSERT INTO public.product_invoice_dto VALUES (148, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 136);
INSERT INTO public.product_invoice_dto VALUES (199, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 188);
INSERT INTO public.product_invoice_dto VALUES (212, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 177);
INSERT INTO public.product_invoice_dto VALUES (213, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 51, 189);
INSERT INTO public.product_invoice_dto VALUES (22, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 19);
INSERT INTO public.product_invoice_dto VALUES (39, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 33);
INSERT INTO public.product_invoice_dto VALUES (37, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 34);
INSERT INTO public.product_invoice_dto VALUES (43, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 36);
INSERT INTO public.product_invoice_dto VALUES (56, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 49);
INSERT INTO public.product_invoice_dto VALUES (23, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 22);
INSERT INTO public.product_invoice_dto VALUES (44, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 37);
INSERT INTO public.product_invoice_dto VALUES (30, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 27);
INSERT INTO public.product_invoice_dto VALUES (32, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 29);
INSERT INTO public.product_invoice_dto VALUES (36, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 34);
INSERT INTO public.product_invoice_dto VALUES (24, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 23);
INSERT INTO public.product_invoice_dto VALUES (45, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 38);
INSERT INTO public.product_invoice_dto VALUES (55, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 49);
INSERT INTO public.product_invoice_dto VALUES (57, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 51);
INSERT INTO public.product_invoice_dto VALUES (29, 250.00, 307.50, 0, 50.00, 5.00, 57.50, 23, 5, 26);
INSERT INTO public.product_invoice_dto VALUES (31, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 28);
INSERT INTO public.product_invoice_dto VALUES (25, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 23);
INSERT INTO public.product_invoice_dto VALUES (34, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 31);
INSERT INTO public.product_invoice_dto VALUES (48, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 41);
INSERT INTO public.product_invoice_dto VALUES (46, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 40);
INSERT INTO public.product_invoice_dto VALUES (52, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, 47);
INSERT INTO public.product_invoice_dto VALUES (54, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 48);
INSERT INTO public.product_invoice_dto VALUES (26, 500.00, 615.00, 0, 100.00, 5.00, 115.00, 23, 38, 24);
INSERT INTO public.product_invoice_dto VALUES (47, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 40);
INSERT INTO public.product_invoice_dto VALUES (58, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 46);
INSERT INTO public.product_invoice_dto VALUES (27, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 21);
INSERT INTO public.product_invoice_dto VALUES (50, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 43);
INSERT INTO public.product_invoice_dto VALUES (51, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 44);
INSERT INTO public.product_invoice_dto VALUES (49, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 50);
INSERT INTO public.product_invoice_dto VALUES (28, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 25);
INSERT INTO public.product_invoice_dto VALUES (33, 2645.00, 3253.35, 0, 500.00, 5.29, 608.35, 23, 2, 30);
INSERT INTO public.product_invoice_dto VALUES (35, 500.00, 615.00, 1, 100.00, 5.00, 115.00, 23, 17, 31);
INSERT INTO public.product_invoice_dto VALUES (4, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 4);
INSERT INTO public.product_invoice_dto VALUES (1, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 1);
INSERT INTO public.product_invoice_dto VALUES (242, 1392.50, 1712.77, 0, 250.00, 5.57, 320.27, 23, 1, 215);
INSERT INTO public.product_invoice_dto VALUES (244, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 4, 216);
INSERT INTO public.product_invoice_dto VALUES (243, 1000.00, 1230.00, 6, 2.00, 500.00, 230.00, 23, 3, 224);
INSERT INTO public.product_invoice_dto VALUES (245, 600.00, 738.00, 6, 1.00, 600.00, 138.00, 23, 10, 217);
INSERT INTO public.product_invoice_dto VALUES (247, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 38, 219);
INSERT INTO public.product_invoice_dto VALUES (246, 137.94, 169.66, 1, 1.00, 137.94, 31.72, 23, 12, 218);
INSERT INTO public.product_invoice_dto VALUES (248, 1000.00, 1230.00, 0, 200.00, 5.00, 230.00, 23, 22, 220);
INSERT INTO public.product_invoice_dto VALUES (250, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 49, 221);
INSERT INTO public.product_invoice_dto VALUES (251, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 8, 223);
INSERT INTO public.product_invoice_dto VALUES (252, 350.00, 430.50, 0, 70.00, 5.00, 80.50, 23, 18, 222);
INSERT INTO public.product_invoice_dto VALUES (253, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 42, 225);
INSERT INTO public.product_invoice_dto VALUES (254, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 52, 227);
INSERT INTO public.product_invoice_dto VALUES (256, 300.00, 369.00, 5, 1.00, 300.00, 69.00, 23, 37, 229);
INSERT INTO public.product_invoice_dto VALUES (258, 284.50, 349.93, 0, 1.00, 284.50, 65.43, 23, 15, 226);
INSERT INTO public.product_invoice_dto VALUES (255, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 43, 228);
INSERT INTO public.product_invoice_dto VALUES (259, 1151.30, 1416.10, 0, 290.00, 3.97, 264.79, 23, 14, 231);
INSERT INTO public.product_invoice_dto VALUES (249, 252.87, 311.03, 5, 1.00, 252.87, 58.16, 23, 12, 218);
INSERT INTO public.product_invoice_dto VALUES (260, 2688.00, 3306.24, 0, 700.00, 3.84, 618.24, 23, 11, 232);
INSERT INTO public.product_invoice_dto VALUES (262, 203.20, 249.93, 0, 1.00, 203.20, 46.73, 23, 44, 233);
INSERT INTO public.product_invoice_dto VALUES (263, 400.00, 492.00, 0, 1.00, 400.00, 92.00, 23, 47, 234);
INSERT INTO public.product_invoice_dto VALUES (261, 925.00, 1137.75, 0, 185.00, 5.00, 212.75, 23, 17, 230);
INSERT INTO public.product_invoice_dto VALUES (265, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 51, 235);
INSERT INTO public.product_invoice_dto VALUES (266, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 41, 236);
INSERT INTO public.product_invoice_dto VALUES (267, 450.00, 553.50, 5, 1.00, 450.00, 103.50, 23, 54, 238);
INSERT INTO public.product_invoice_dto VALUES (268, 400.00, 492.00, 0, 80.00, 5.00, 92.00, 23, 6, 237);
INSERT INTO public.product_invoice_dto VALUES (269, 800.00, 984.00, 6, 1.00, 800.00, 184.00, 23, 50, 240);
INSERT INTO public.product_invoice_dto VALUES (271, 750.00, 922.50, 6, 750.00, 1.00, 172.50, 23, 57, 241);
INSERT INTO public.product_invoice_dto VALUES (272, 450.00, 553.50, 5, 1.00, 450.00, 103.50, 23, 58, 242);
INSERT INTO public.product_invoice_dto VALUES (270, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 56, 244);
INSERT INTO public.product_invoice_dto VALUES (275, 750.00, 922.50, 6, 1.00, 750.00, 172.50, 23, 55, 239);
INSERT INTO public.product_invoice_dto VALUES (274, 400.00, 492.00, 5, 1.00, 400.00, 92.00, 23, 59, 243);
INSERT INTO public.product_invoice_dto VALUES (257, 140.00, 172.20, 0, 4.00, 35.00, 32.20, 23, 37, 229);
INSERT INTO public.product_invoice_dto VALUES (264, 350.00, 430.50, 5, 1.00, 350.00, 80.50, 23, 17, 230);
INSERT INTO public.product_invoice_dto VALUES (273, 350.00, 430.50, 0, 7.00, 50.00, 80.50, 23, 56, 244);


--
-- TOC entry 3421 (class 0 OID 16420)
-- Dependencies: 228
-- Data for Name: productdescription; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.productdescription VALUES (2, NULL, 24, 'a4', 'a4', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (3, NULL, 24, 'a2', 'a2', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (1, NULL, 0, 'a1', 'a1', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (4, NULL, NULL, 'a3', 'a3', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (5, NULL, NULL, 'a5', 'a5', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (6, NULL, NULL, 'a6', 'a6', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (7, NULL, NULL, 'a7', 'a7', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (8, NULL, NULL, 'a8', 'a8', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (9, NULL, NULL, 'a9', 'a9', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (10, NULL, 24, 'B1', 'b1', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (11, NULL, NULL, 'B2', 'b2', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (12, NULL, NULL, 'B3', 'b3', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (13, NULL, NULL, 'B4', 'b4', NULL, NULL, '{}');
INSERT INTO public.productdescription VALUES (14, NULL, NULL, 'B5', 'b5
', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (15, NULL, NULL, 'B6', 'b6', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (16, NULL, NULL, 'b7', 'b7', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (17, NULL, NULL, 'b8', 'b8', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (18, NULL, NULL, 'b9', 'b9', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (19, NULL, NULL, 'B10', 'b10', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (20, NULL, NULL, 'C1', 'c1
', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (21, NULL, NULL, 'c2', 'c2', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (22, NULL, NULL, 'c3', 'c3', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (23, NULL, NULL, 'c4', 'c4', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (24, NULL, NULL, 'd1', 'd1', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (25, NULL, NULL, 'd2', 'd2', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (26, NULL, NULL, 'd3', 'd3', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (27, NULL, NULL, 'd4', 'd4', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (28, NULL, NULL, 'd5', 'd5', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (29, NULL, NULL, 'd6', 'd6', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (30, NULL, NULL, 'd7', 'd7', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (31, NULL, NULL, 'd8', 'd8', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (32, NULL, NULL, 'd9', 'd9', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (33, NULL, NULL, 'd10', 'd10', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (34, NULL, NULL, 'e1', 'e1', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (35, NULL, NULL, 'e2', 'e2', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (36, NULL, NULL, 'e3', 'e3', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (37, NULL, NULL, 'e4', 'e4', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (38, NULL, NULL, 'e5', 'e5', NULL, NULL, NULL);
INSERT INTO public.productdescription VALUES (39, NULL, NULL, 'e6', 'e6', NULL, NULL, NULL);


--
-- TOC entry 3423 (class 0 OID 16426)
-- Dependencies: 230
-- Data for Name: user_login; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.user_login VALUES (1, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (2, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (3, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (4, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (5, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (6, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (7, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (8, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (9, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (10, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (11, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (12, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (13, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (14, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (15, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (16, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (17, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (18, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (19, 'user1', 'user1', 'USER', false, false, false, true, '', NULL, 'testNameUSER', NULL, NULL, NULL);
INSERT INTO public.user_login VALUES (20, 'admin1', 'admin1', 'ADMIN', false, false, false, true, '8942957044', NULL, 'testNameADMIN', NULL, NULL, NULL);


--
-- TOC entry 3438 (class 0 OID 0)
-- Dependencies: 218
-- Name: address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.address_id_seq', 57, true);


--
-- TOC entry 3439 (class 0 OID 0)
-- Dependencies: 220
-- Name: client_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.client_id_seq', 59, true);


--
-- TOC entry 3440 (class 0 OID 0)
-- Dependencies: 223
-- Name: invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.invoice_id_seq', 244, true);


--
-- TOC entry 3441 (class 0 OID 0)
-- Dependencies: 225
-- Name: product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_id_seq', 67, true);


--
-- TOC entry 3442 (class 0 OID 0)
-- Dependencies: 227
-- Name: product_invoice_dto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_invoice_dto_id_seq', 275, true);


--
-- TOC entry 3443 (class 0 OID 0)
-- Dependencies: 229
-- Name: productdescription_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.productdescription_id_seq', 2, true);


--
-- TOC entry 3444 (class 0 OID 0)
-- Dependencies: 231
-- Name: user_login_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_login_id_seq', 20, true);


--
-- TOC entry 3252 (class 2606 OID 16440)
-- Name: address address_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);


--
-- TOC entry 3254 (class 2606 OID 16442)
-- Name: client client_nip_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_nip_key UNIQUE (nip);


--
-- TOC entry 3256 (class 2606 OID 16444)
-- Name: client client_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);


--
-- TOC entry 3258 (class 2606 OID 16446)
-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);


--
-- TOC entry 3262 (class 2606 OID 16448)
-- Name: product_invoice_dto product_invoice_dto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_invoice_dto
    ADD CONSTRAINT product_invoice_dto_pkey PRIMARY KEY (id);


--
-- TOC entry 3260 (class 2606 OID 16450)
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- TOC entry 3264 (class 2606 OID 16452)
-- Name: productdescription productdescription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productdescription
    ADD CONSTRAINT productdescription_pkey PRIMARY KEY (id);


-- Completed on 2025-07-02 12:53:23

--
-- PostgreSQL database dump complete
--

