INSERT INTO test1 (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (1, 'One', 7.4, null, 0, 1, 'n1', 12.65, 's1', null);
INSERT INTO test1 (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (2, 'Two', null, parsedatetime('19-04-2016', 'dd-MM-yyyy'), 1, 0, 'n2', 3, 's2', parsedatetime('19-04-2016', 'dd-MM-yyyy'));

INSERT INTO test2 (code,sequence) VALUES ('CODE1', 1);
INSERT INTO test2 (code,sequence) VALUES ('CODE2', 7);

INSERT INTO test1bis (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (1, 'One', 7.4, null, 0, 1, 'n1', 12.65, 's1', null);
INSERT INTO test1bis (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (2, 'Two', 5, null, 1, 0, 'n2', 3, 's2', null);
INSERT INTO test1bis (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (3, 'Three', 5, null, 1, 0, 'n1', 3, 's3', null);
INSERT INTO test1bis (keycode,strv,decv,datv,enmv,nbv,nst1,nst2,nss1,nss2) VALUES (4, 'Three', 6, null, 0, 1, 'n1', 4, 's1', null);
