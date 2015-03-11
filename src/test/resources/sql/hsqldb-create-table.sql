












CREATE TABLE IF NOT EXISTS app (
  id integer NOT NULL IDENTITY,
  name varchar(200) NOT NULL ,
  app_id varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,
  
  constraint name unique (name)
) ;





CREATE TABLE IF NOT EXISTS app_health_check (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  uri varchar(200) NOT NULL ,
  intervals int(11) NOT NULL ,
  fails int(11) NOT NULL ,
  passes int(11) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint application_id unique (app_id)
) ;





CREATE TABLE IF NOT EXISTS app_load_balancing_method (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  type varchar(100) NOT NULL ,
  value varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint application_id unique (app_id)
) ;





CREATE TABLE IF NOT EXISTS app_server (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  ip varchar(200) NOT NULL ,
  port int(11) NOT NULL ,
  weight int(11) NOT NULL ,
  max_fails int(11) NOT NULL ,
  fail_timeout int(11) NOT NULL ,
  healthy bit(1) NOT NULL ,
  enable bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint app_id_ip unique (app_id,ip)
) ;





CREATE TABLE IF NOT EXISTS app_slb (
  id integer NOT NULL IDENTITY,
  app_id integer NOT NULL ,
  slb_name varchar(200) NOT NULL ,
  slb_virtual_server_name varchar(200) NOT NULL ,
  path varchar(200) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint app_id_slb_name_slb_virtual_server_name unique (app_id,slb_name,slb_virtual_server_name)
) ;





CREATE TABLE IF NOT EXISTS server (
  id integer NOT NULL IDENTITY,
  ip varchar(100) NOT NULL ,
  host_name varchar(100) NOT NULL ,
  up bit(1) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint ip unique (ip)
) ;





CREATE TABLE IF NOT EXISTS slb (
  id integer NOT NULL IDENTITY,
  name varchar(100) NOT NULL ,
  nginx_bin varchar(300) NOT NULL ,
  nginx_conf varchar(300) NOT NULL ,
  nginx_worker_processes int(11) NOT NULL ,
  status varchar(300) NOT NULL ,
  created_time timestamp NULL ,
  last_modified timestamp NOT NULL ,

  constraint name unique (name)
) ;


