create table widget
(
    id varchar(36) not null primary key,
    x int not null,
    y int not null,
    z int not null unique,
    width int not null,
    height int not null,
    lastModified timestamp with time zone not null
);
