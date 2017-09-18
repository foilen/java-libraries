
    create table machine (
       id bigint not null auto_increment,
        name varchar(255) not null,
        version bigint not null,
        primary key (id)
    ) type=InnoDB;

    create table machine_ips (
       machine_id bigint not null,
        ips varchar(255)
    ) type=InnoDB;

    create table machine_statisticfs (
       id bigint not null auto_increment,
        is_root bit not null,
        path longtext,
        total_space bigint not null,
        used_space bigint not null,
        primary key (id)
    ) type=InnoDB;

    create table machine_statistic_network (
       id bigint not null auto_increment,
        in_bytes bigint not null,
        interface_name varchar(255),
        out_bytes bigint not null,
        primary key (id)
    ) type=InnoDB;

    create table machine_statistics (
       id bigint not null auto_increment,
        aggregations_for_day integer not null,
        aggregations_for_hour integer not null,
        cpu_total bigint not null,
        cpu_used bigint not null,
        memory_swap_total bigint not null,
        memory_swap_used bigint not null,
        memory_total bigint not null,
        memory_used bigint not null,
        timestamp datetime,
        machine_id bigint not null,
        primary key (id)
    ) type=InnoDB;

    create table machine_statistics_fs (
       machine_statistics_id bigint not null,
        fs_id bigint not null,
        primary key (machine_statistics_id, fs_id)
    ) type=InnoDB;

    create table machine_statistics_networks (
       machine_statistics_id bigint not null,
        networks_id bigint not null,
        primary key (machine_statistics_id, networks_id)
    ) type=InnoDB;

    alter table machine 
       add constraint UK_iy15tft5g7qcqkfxicfxy3f2y unique (name);

    alter table machine_statistics_fs 
       add constraint UK_9dmogrrotqudkwogn3jxow1o7 unique (fs_id);

    alter table machine_statistics_networks 
       add constraint UK_jmuvbuf1gkt6xygfmfvt2r4f unique (networks_id);

    alter table machine_ips 
       add constraint FKl2wxtyd612uk9pfrcg5epy8hu 
       foreign key (machine_id) 
       references machine (id);

    alter table machine_statistics 
       add constraint FKiln1f7l62nhj0y8irvtte3uau 
       foreign key (machine_id) 
       references machine (id);

    alter table machine_statistics_fs 
       add constraint FKs8ufpfbcnioxn4ps7cgkqma81 
       foreign key (fs_id) 
       references machine_statisticfs (id);

    alter table machine_statistics_fs 
       add constraint FK8bov3os0rkykiujxpwc2s9lqd 
       foreign key (machine_statistics_id) 
       references machine_statistics (id);

    alter table machine_statistics_networks 
       add constraint FK5pvvn1w3in92q14tyxvhi1lf4 
       foreign key (networks_id) 
       references machine_statistic_network (id);

    alter table machine_statistics_networks 
       add constraint FK28t7df9et3q9yadbluum71a4k 
       foreign key (machine_statistics_id) 
       references machine_statistics (id);
