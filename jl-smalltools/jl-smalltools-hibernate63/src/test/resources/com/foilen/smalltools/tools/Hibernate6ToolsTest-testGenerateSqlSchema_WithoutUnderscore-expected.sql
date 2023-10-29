
    create table Machine (
        id bigint not null auto_increment,
        name varchar(255) not null,
        version bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table Machine_ips (
        Machine_id bigint not null,
        ips varchar(255)
    ) engine=InnoDB;

    create table MachineStatisticFS (
        id bigint not null auto_increment,
        isRoot bit not null,
        path varchar(2000),
        totalSpace bigint not null,
        usedSpace bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table MachineStatisticNetwork (
        id bigint not null auto_increment,
        inBytes bigint not null,
        interfaceName varchar(255),
        outBytes bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table MachineStatistics (
        id bigint not null auto_increment,
        aggregationsForDay integer not null,
        aggregationsForHour integer not null,
        cpuTotal bigint not null,
        cpuUsed bigint not null,
        memorySwapTotal bigint not null,
        memorySwapUsed bigint not null,
        memoryTotal bigint not null,
        memoryUsed bigint not null,
        timestamp datetime(6),
        machine_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table MachineStatistics_MachineStatisticFS (
        MachineStatistics_id bigint not null,
        fs_id bigint not null,
        primary key (MachineStatistics_id, fs_id)
    ) engine=InnoDB;

    create table MachineStatistics_MachineStatisticNetwork (
        MachineStatistics_id bigint not null,
        networks_id bigint not null,
        primary key (MachineStatistics_id, networks_id)
    ) engine=InnoDB;

    alter table Machine 
       add constraint UK_n6n02m3sthemy1vpndvhiqcsx unique (name);

    alter table MachineStatistics 
       add constraint UK_bu23yo359n592a4mx72datqfw unique (machine_id);

    alter table MachineStatistics_MachineStatisticFS 
       add constraint UK_9pdvpwxb1quuqdle1qympo5t unique (fs_id);

    alter table MachineStatistics_MachineStatisticNetwork 
       add constraint UK_debineidldpdya44fhqdfl3uv unique (networks_id);

    alter table Machine_ips 
       add constraint FKjt7yd6yj5f1g6pq12n7sxyfyl 
       foreign key (Machine_id) 
       references Machine (id);

    alter table MachineStatistics 
       add constraint FKt72ltqvmrxquhq6y17e4i4cgf 
       foreign key (machine_id) 
       references Machine (id);

    alter table MachineStatistics_MachineStatisticFS 
       add constraint FKhnix4as0yasybga1qn4ib8prb 
       foreign key (fs_id) 
       references MachineStatisticFS (id);

    alter table MachineStatistics_MachineStatisticFS 
       add constraint FK2si4iarv1sjc6vyxmoyub1wrp 
       foreign key (MachineStatistics_id) 
       references MachineStatistics (id);

    alter table MachineStatistics_MachineStatisticNetwork 
       add constraint FKdcoa5ylhm921n6bhtm1pacbu3 
       foreign key (networks_id) 
       references MachineStatisticNetwork (id);

    alter table MachineStatistics_MachineStatisticNetwork 
       add constraint FK3072wwwbvl7xdg66n0jdd9bq6 
       foreign key (MachineStatistics_id) 
       references MachineStatistics (id);
