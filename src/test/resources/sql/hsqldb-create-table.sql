












CREATE TABLE IF NOT EXISTS app (
  id integer NOT NULL IDENTITY,
  name varchar(200) NOT NULL ,
  app_id varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_name unique (name)
) ;





CREATE TABLE IF NOT EXISTS app_health_check (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  uri varchar(200) NOT NULL ,
  intervals int NOT NULL ,
  fails int NOT NULL ,
  passes int NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_application_id unique (app_id)
) ;





CREATE TABLE IF NOT EXISTS app_load_balancing_method (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  type varchar(100) NOT NULL ,
  value varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_application_id2 unique (app_id)
) ;





CREATE TABLE IF NOT EXISTS app_server (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  ip varchar(200) NOT NULL ,
  port int NOT NULL ,
  weight int NOT NULL ,
  max_fails int NOT NULL ,
  fail_timeout int NOT NULL ,
  healthy bit(1) NOT NULL ,
  enable bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_app_id_ip unique (app_id,ip)
) ;





CREATE TABLE IF NOT EXISTS app_slb (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  slb_name varchar(200) NOT NULL ,
  slb_virtual_server_name varchar(200) NOT NULL ,
  path varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_app_id_slb_name_slb_virtual_server_name unique (app_id,slb_name,slb_virtual_server_name)
) ;





CREATE TABLE IF NOT EXISTS server (
  id integer NOT NULL IDENTITY,
  ip varchar(100) NOT NULL ,
  host_name varchar(100) NOT NULL ,
  up bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_ip unique (ip)
) ;





CREATE TABLE IF NOT EXISTS slb (
  id integer NOT NULL IDENTITY,
  name varchar(100) NOT NULL ,
  nginx_bin varchar(300) NOT NULL ,
  nginx_conf varchar(300) NOT NULL ,
  nginx_worker_processes int NOT NULL ,
  status varchar(300) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_name2 unique (name)
) ;





CREATE TABLE IF NOT EXISTS slb_domain (
  id integer NOT NULL IDENTITY,
  slb_virtual_server_id integer NOT NULL ,
  name varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_slb_virtual_server_id_name unique (slb_virtual_server_id,name)
) ;





CREATE TABLE IF NOT EXISTS slb_server (
  id integer NOT NULL IDENTITY,
  slb_id integer NOT NULL ,
  ip varchar(50) NOT NULL ,
  host_name varchar(200) NOT NULL ,
  enable bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_ip2 unique (ip)
) ;





CREATE TABLE IF NOT EXISTS slb_vip (
  id integer NOT NULL IDENTITY,
  slb_id integer NOT NULL ,
  ip varchar(50) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_ip3 unique (ip)
) ;





CREATE TABLE IF NOT EXISTS slb_virtual_server (
  id integer NOT NULL IDENTITY,
  slb_id integer NOT NULL ,
  name varchar(200) NOT NULL ,
  port varchar(200) NOT NULL ,
  is_ssl bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint con_slb_id_name unique (slb_id,name)
) ;





