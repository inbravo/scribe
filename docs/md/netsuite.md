###		<b>NetSuite</b> CRM examples :+1:

####	Search all 'contact'
```
http://localhost:8080/scribe/object/contact?[CRM-MetaObject-Info]
```
####	Search 'contact' by a single Query Criteria (phone number)
```
http://localhost:8080/scribe/object/contact/Phone___SearchStringField%20is%204083380698/?[CRM-MetaObject-Info]
```
####	Search 'contact' by multple Query Criterias (Phone and LastName)
```
http://localhost:8080/scribe/object/contact/Phone___SearchStringField%20is%204083380698&LastName___SearchStringField%20contains%20Martin|Address___SearchStringField%20contains%204083380698/?[CRM-MetaObject-Info]
```
####	CRM Meta Object is required for your NetSute CRM account details

Replace [CRM-MetaObject-Info] with following string after relacing all '[]' values with your CRM credentials
```
MetaObject.CrmUserId=[Your-CRM-User-Id]&MetaObject.CrmPassword=[Your-CRM-Password]&MetaObject.CrmType=NS&MetaObject.CrmAccountId=[Your-CRM-Account-Id]
```

- MetaObject.CrmUserId need your CRM user id
- MetaObject.CrmPassword is your CRM password
- MetaObject.CrmType is your CRM type, which is 'NS' in case of NetSuite CRM
- MetaObject.CrmAccountId is your <b>NetSuite</b> CRM account id

####	Format of NetSuite CRM Object Fields Query Criteria (e.g. Phone___SearchStringField)
Phone is the name of Field and SearchStringField is the type of field. This info is separated by three underscores  <b>(___) </b>
######	Following are the examples for all fields of NetSuite CRM object 'Contact', which can be used for Query Criteria
-	AccountNumber___SearchStringField
-	Address___SearchStringField
-	Addressee___SearchStringField
-	AddressLabel___SearchStringField
-	AddressPhone___SearchStringField
-	Attention___SearchStringField
-	AvailableOffline___SearchBooleanField
-	Balance___SearchDoubleField
-	BillAddress___SearchStringField
-	BoughtAmount___SearchDoubleField
-	BoughtDate___SearchDateField
-	BuyingReason___SearchMultiSelectField
-	BuyingTimeFrame___SearchMultiSelectField
-	Category___SearchMultiSelectField
-	CcCustomerCode___SearchStringField
-	CcDefault___SearchBooleanField
-	CcExpDate___SearchDateField
-	CcHolderName___SearchStringField
-	CcNumber___SearchStringField
-	CcType___SearchMultiSelectField
-	City___SearchStringField
-	ClassBought___SearchMultiSelectField
-	Comments___SearchStringField
-	CompanyName___SearchStringField
-	ConsolBalance___SearchDoubleField
-	ConsolDaysOverdue___SearchLongField
-	ConsolDepositBalance___SearchDoubleField
-	ConsolOverdueBalance___SearchDoubleField
-	ConsolUnbilledOrders___SearchDoubleField
-	Contact___SearchStringField
-	Contribution___SearchLongField
-	ConversionDate___SearchDateField
-	Country___SearchEnumMultiSelectField
-	County___SearchStringField
-	CreditHoldOverride___SearchBooleanField
-	CreditLimit___SearchDoubleField
-	Currency___SearchMultiSelectField
-	CustStage___SearchMultiSelectField
-	CustStatus___SearchMultiSelectField
-	DateClosed___SearchDateField
-	DateCreated___SearchDateField
-	DaysOverdue___SearchLongField
-	DepositBalance___SearchDoubleField
-	DeptBought___SearchMultiSelectField
-	Email___SearchStringField
-	EmailPreference___SearchEnumMultiSelectField
-	EmailTransactions___SearchBooleanField
-	EndDate___SearchDateField
-	EntityId___SearchStringField
-	EntityStatus___SearchMultiSelectField
-	EstimatedBudget___SearchDoubleField
-	ExplicitConversion___SearchBooleanField
-	ExternalId___SearchMultiSelectField
-	ExternalIdString___SearchStringField
-	Fax___SearchStringField
-	FaxTransactions___SearchBooleanField
-	FirstName___SearchStringField
-	FirstOrderDate___SearchDateField
-	FirstSaleDate___SearchDateField
-	FxBalance___SearchDoubleField
-	FxConsolBalance___SearchDoubleField
-	FxConsolUnbilledOrders___SearchDoubleField
-	FxUnbilledOrders___SearchDoubleField
-	GiveAccess___SearchBooleanField
-	GlobalSubscriptionStatus___SearchEnumMultiSelectField
-	Group___SearchMultiSelectField
-	GroupPricingLevel___SearchMultiSelectField
-	HasDuplicates___SearchBooleanField
-	Image___SearchStringField
-	InternalId___SearchMultiSelectField
-	InternalIdNumber___SearchLongField
-	IsBudgetApproved___SearchBooleanField
-	IsDefaultBilling___SearchBooleanField
-	IsDefaultShipping___SearchBooleanField
-	IsInactive___SearchBooleanField
-	IsPerson___SearchBooleanField
-	IsReportedLead___SearchBooleanField
-	IsShipAddress___SearchBooleanField
-	ItemPricingLevel___SearchMultiSelectField
-	ItemPricingUnitPrice___SearchDoubleField
-	ItemsBought___SearchMultiSelectField
-	ItemsOrdered___SearchMultiSelectField
-	Language___SearchEnumMultiSelectField
-	LastModifiedDate___SearchDateField
-	LastName___SearchStringField
-	LastOrderDate___SearchDateField
-	LastSaleDate___SearchDateField
-	LeadDate___SearchDateField
-	LeadSource___SearchMultiSelectField
-	Level___SearchEnumMultiSelectField
-	LocationBought___SearchMultiSelectField
-	ManualCreditHold___SearchBooleanField
-	MerchantAccount___SearchMultiSelectField
-	MiddleName___SearchStringField
-	MonthlyClosing___SearchEnumMultiSelectField
-	OnCreditHold___SearchBooleanField
-	OrderedAmount___SearchDoubleField
-	OrderedDate___SearchDateField
-	OtherRelationships___SearchEnumMultiSelectField
-	OverdueBalance___SearchDoubleField
-	Parent___SearchMultiSelectField
-	ParentItemsBought___SearchMultiSelectField
-	ParentItemsOrdered___SearchMultiSelectField
-	Partner___SearchMultiSelectField
-	PartnerContribution___SearchLongField
-	PartnerRole___SearchMultiSelectField
-	PartnerTeamMember___SearchMultiSelectField
-	Pec___SearchStringField
-	Permission___SearchEnumMultiSelectField
-	Phone___SearchStringField
-	PhoneticName___SearchStringField
-	PriceLevel___SearchMultiSelectField
-	PricingGroup___SearchMultiSelectField
-	PricingItem___SearchMultiSelectField
-	PrintTransactions___SearchBooleanField
-	ProspectDate___SearchDateField
-	PstExempt___SearchBooleanField
-	ReminderDate___SearchDateField
-	ResaleNumber___SearchStringField
-	Role___SearchMultiSelectField
-	SalesReadiness___SearchMultiSelectField
-	SalesRep___SearchMultiSelectField
-	SalesTeamMember___SearchMultiSelectField
-	SalesTeamRole___SearchMultiSelectField
-	Salutation___SearchStringField
-	ShipAddress___SearchStringField
-	ShipComplete___SearchBooleanField
-	ShippingItem___SearchMultiSelectField
-	Stage___SearchEnumMultiSelectField
-	StartDate___SearchDateField
-	State___SearchStringField
-	SubsidBought___SearchMultiSelectField
-	Subsidiary___SearchMultiSelectField
-	Taxable___SearchBooleanField
-	Terms___SearchMultiSelectField
-	Territory___SearchMultiSelectField
-	Title___SearchStringField
-	UnbilledOrders___SearchDoubleField
-	Url___SearchStringField
-	VatRegNumber___SearchStringField
-	WebLead___SearchBooleanField
-	ZipCode___SearchStringField