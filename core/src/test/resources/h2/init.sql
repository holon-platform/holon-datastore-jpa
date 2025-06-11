/* schema */

create table test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv double 
	,datv date 
	,datv2 date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) default 'nst1'
	,nst2 decimal(14,4)
	,nss1 varchar(10) 
	,tms timestamp
	,tms2 timestamp
	,tm time
	,clb clob
	,blb blob
);

create table test2 (
	 code bigint primary key auto_increment
	,text varchar(100) not null
);

create table test3 (
	 code bigint not null primary key
	,text varchar(100) not null
);

create table test_recur (
	 code bigint primary key auto_increment
	,name varchar(100) not null
	,parent varchar(100)
);

create table test_nopk (
	nmb numeric(10),
	txt varchar(10)
);

create table testtx (
	code bigint primary key, 
	text varchar(100) not null
); 

/* data */

INSERT INTO test1 VALUES (1, 'One', 7.4, parsedatetime('19-05-2016', 'dd-MM-yyyy'), parsedatetime('19-05-2016', 'dd-MM-yyyy'), 0, 1, 'n1', 12.65, 's1', null, null, '18:30:15', 'clocbcontent', x'C9CBBBCCCEB9C8CABCCCCEB9C9CBBB');
INSERT INTO test1 VALUES (2, 'Two', null, parsedatetime('19-04-2016', 'dd-MM-yyyy'), parsedatetime('19-04-2016', 'dd-MM-yyyy'), 1, 0, 'n2', 3, 's2', parsedatetime('23-03-2017 15:30:25', 'dd-MM-yyyy HH:mm:ss'), parsedatetime('23-03-2017 15:30:25', 'dd-MM-yyyy HH:mm:ss'), null, 'clocbcontent', null);

INSERT INTO test3 VALUES (2, 'TestJoin');
INSERT INTO test3 VALUES (3, 'TestJoin3');

INSERT INTO test_recur (name, parent) VALUES ('test1', null);
INSERT INTO test_recur (name, parent) VALUES ('test2', 'test1');
INSERT INTO test_recur (name, parent) VALUES ('test3', 'test2');

INSERT INTO test_nopk (nmb, txt) VALUES (1, 'First');
INSERT INTO test_nopk (nmb, txt) VALUES (2, 'Second');

INSERT INTO testtx VALUES (1, 'TheOne');

commit;

