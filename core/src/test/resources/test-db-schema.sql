create table if not exists test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv double 
	,datv date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) 
	,nst2 numeric(19,2) 
	,nss1 varchar(10) 
	,nss2 timestamp
);

create table if not exists test1bis (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv double 
	,datv date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) 
	,nst2 numeric(19,2) 
	,nss1 varchar(10) 
	,nss2 timestamp
);

create table if not exists test2 (
	 code varchar(100) primary key 
	,sequence bigint 
);

create table if not exists test3 (
	 code bigint auto_increment PRIMARY KEY
	,value varchar(10)
);

create table if not exists test_recur (
	 code bigint primary key auto_increment
	,name varchar(100) not null
	,parent varchar(100)
);

create table if not exists testtp (
	 id bigint primary key 
	,datv date 
	,tmsv timestamp
	,ldatv date 
	,ltmsv timestamp
);


