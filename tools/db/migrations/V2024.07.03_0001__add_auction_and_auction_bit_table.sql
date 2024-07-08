   create TABLE IF NOT EXISTS sk_auction
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(8) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(256) NOT NULL,
    offer INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE NOT NULL,
    highest_bid INTEGER NOT NULL,
    highest_bidder_id NOT NULL,
    hignest_bidder_name VARCHAR(128) NOT NULL,
    status VARCHAR(16),
    created_by INT NOT NULL,
    updated_by INT,
    deleted_by INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

create table IF NOT EXISTS sk_auction_bit
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bid INTEGER NOT NULL,
    bidder INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);