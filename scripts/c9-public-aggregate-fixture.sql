-- Public fictional SQL aggregate fixture only; loaded into the initially empty
-- third guarded disposable server, never the original or restored database.
START TRANSACTION;
INSERT INTO organization(id,public_id,slug,display_name,status,created_at,updated_at) VALUES
(1,UUID_TO_BIN(UUID(),0),'projection-alpha','Fictional projection alpha','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)),
(2,UUID_TO_BIN(UUID(),0),'projection-beta','Fictional projection beta','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO user_account(id,public_id,username_key,display_name,status,created_at,updated_at) VALUES
(1,UUID_TO_BIN(UUID(),0),'additive-original-owner','Fictional projection owner','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)),
(2,UUID_TO_BIN(UUID(),0),'projection-other','Fictional projection other','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO membership(id,public_id,org_id,user_id,role,status,created_at,updated_at) VALUES
(1,UUID_TO_BIN(UUID(),0),1,1,'OWNER','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)),
(2,UUID_TO_BIN(UUID(),0),2,2,'OWNER','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO department(id,organization_id,name,name_key) VALUES
(1,1,'Projection','projection'),(2,2,'Projection','projection');
INSERT INTO project(id,public_id,organization_id,department_id,name,start_at,end_at) VALUES
(1,UUID_TO_BIN(UUID(),0),1,1,'Projection','2026-01-01','2026-01-02'),
(2,UUID_TO_BIN(UUID(),0),2,2,'Projection','2026-01-01','2026-01-02');
INSERT INTO resource(id,public_id,organization_id,name,name_key,type,total_quantity,version) VALUES
(1,UUID_TO_BIN(UUID(),0),1,'Projection supply','projection supply','REUSABLE',10,4),
(2,UUID_TO_BIN(UUID(),0),1,'Projection paper','projection paper','CONSUMABLE',10,2),
(3,UUID_TO_BIN(UUID(),0),2,'Projection supply','projection supply','CONSUMABLE',7,2);
INSERT INTO resource_requirement(id,public_id,organization_id,project_id,resource_id,quantity) VALUES
(1,UUID_TO_BIN(UUID(),0),1,1,1,12),
(2,UUID_TO_BIN(UUID(),0),1,1,2,15),
(3,UUID_TO_BIN(UUID(),0),2,2,3,9);
INSERT INTO allocation(id,public_id,organization_id,requirement_id,resource_id,quantity,borrower_name,status,created_at,approved_at,issued_at,returned_at,consumed_at) VALUES
(1,UUID_TO_BIN(UUID(),0),1,1,1,3,'Fictional borrower','ISSUED','2026-01-01','2026-01-01','2026-01-01',NULL,NULL),
(2,UUID_TO_BIN(UUID(),0),1,1,1,1,'Fictional borrower','RETURNED','2026-01-01','2026-01-01','2026-01-01','2026-01-02',NULL),
(3,UUID_TO_BIN(UUID(),0),1,2,2,5,'Fictional borrower','CONSUMED','2026-01-01','2026-01-01',NULL,NULL,'2026-01-02'),
(4,UUID_TO_BIN(UUID(),0),1,2,2,2,'Fictional borrower','APPROVED','2026-01-01','2026-01-01',NULL,NULL,NULL),
(5,UUID_TO_BIN(UUID(),0),1,2,2,3,'Fictional borrower','APPROVED','2026-01-01','2026-01-01',NULL,NULL,NULL),
(6,UUID_TO_BIN(UUID(),0),2,3,3,2,'Fictional borrower','CONSUMED','2026-01-01','2026-01-01',NULL,NULL,'2026-01-02');
INSERT INTO stock_movement(public_id,organization_id,resource_id,allocation_id,actor_membership_id,operation_public_id,request_fingerprint,kind,quantity,delta_total,delta_issued,occurred_at) VALUES
(UUID_TO_BIN(UUID(),0),1,1,NULL,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-1',256)),'INITIAL',12,12,0,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),1,1,NULL,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-2',256)),'ADJUST',2,-2,0,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),1,1,1,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-3',256)),'ISSUE',3,0,3,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),1,1,2,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-4',256)),'ISSUE',1,0,1,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),1,1,2,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-5',256)),'RETURN',1,0,-1,'2026-01-02'),
(UUID_TO_BIN(UUID(),0),1,2,NULL,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-6',256)),'INITIAL',15,15,0,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),1,2,3,1,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-7',256)),'CONSUME',5,-5,0,'2026-01-02'),
(UUID_TO_BIN(UUID(),0),2,3,NULL,2,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-8',256)),'INITIAL',9,9,0,'2026-01-01'),
(UUID_TO_BIN(UUID(),0),2,3,6,2,UUID_TO_BIN(UUID(),0),UNHEX(SHA2('fictional-9',256)),'CONSUME',2,-2,0,'2026-01-02');
COMMIT;
