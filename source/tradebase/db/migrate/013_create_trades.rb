require "migrate_plus"

class CreateTrades < ActiveRecord::Migration
  include MigratePlus

  def self.up
    create_table :trades do |t|
      t.column :quantity, :float
      t.column :side, :integer
      t.column :trade_type, :string, :limit=>1
      t.column :journal_id, :integer
      t.column :account_id, :integer, :null => false
      t.column :comment, :string, :limit=>255
      
      t.column :tradeable_id, :integer
      t.column :tradeable_type, :string
      
      t.column :created_on, :datetime
      t.column :updated_on, :datetime
    end
    add_fkey :trades, :journal_id, :journals
    add_fkey :trades, :account_id, :accounts
    
  end

  def self.down
    drop_table :trades
  end
end
