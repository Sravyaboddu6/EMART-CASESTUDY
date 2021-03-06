package com.project.emart.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.emart.dao.BillDao;
import com.project.emart.dao.BillDetailsDao;
import com.project.emart.entity.BillDetailsEntity;
import com.project.emart.entity.BillEntity;
import com.project.emart.entity.BuyerSignupEntity;
import com.project.emart.entity.CategoryEntity;
import com.project.emart.entity.ItemEntity;
import com.project.emart.entity.SellerSignupEntity;
import com.project.emart.entity.SubCategoryEntity;
import com.project.emart.pojo.BillDetailsPojo;
import com.project.emart.pojo.BillPojo;
import com.project.emart.pojo.BuyerSignupPojo;
import com.project.emart.pojo.CategoryPojo;
import com.project.emart.pojo.ItemPojo;
import com.project.emart.pojo.SellerSignupPojo;
import com.project.emart.pojo.SubCategoryPojo;

@Service
public class BillServiceImpl implements BillService {
	static Logger LOG = Logger.getLogger(BillServiceImpl.class.getClass());
	
	
	@Autowired
	BillDao billDao;

	@Autowired
	BillDetailsDao billDetailsDao;

	@Override
	@Transactional
	
	//adding bill into the database from the received bill pojo
	
	public BillPojo addBill(BillPojo billPojo) {

		
		LOG.info("entered addBill()");
		
		BuyerSignupPojo buyerSignupPojo = billPojo.getBuyer();
		BuyerSignupEntity buyerSignupEntity = new BuyerSignupEntity(buyerSignupPojo.getId(),
				buyerSignupPojo.getUsername(), buyerSignupPojo.getPassword(), buyerSignupPojo.getEmail(),
				buyerSignupPojo.getMobile(), buyerSignupPojo.getDate(), null);

		BillEntity billEntity = new BillEntity();
		billEntity.setId(0);
		billEntity.setAmount(billPojo.getAmount());
		billEntity.setRemarks(billPojo.getRemarks());
		billEntity.setType(billPojo.getType());
		billEntity.setDate(billPojo.getDate());
		billEntity.setBuyer(buyerSignupEntity);

		billEntity = billDao.saveAndFlush(billEntity);
		billPojo.setId(billEntity.getId());

		BillEntity setbillEntity = billDao.findById(billEntity.getId()).get();
		Set<BillDetailsEntity> allBillDetails = new HashSet<BillDetailsEntity>();
		Set<BillDetailsPojo> allBillDetailsPojo = billPojo.getAllBillDetails();
		
		//copying all pojo contents into respective entities

		for (BillDetailsPojo billDetailsPojo : allBillDetailsPojo) {
			ItemPojo itemPojo = billDetailsPojo.getItem();
			SubCategoryPojo subCategoryPojo = itemPojo.getSubCategory();
			CategoryPojo categoryPojo = subCategoryPojo.getCategory();
			SellerSignupPojo sellerSignupPojo = itemPojo.getSeller();

			SellerSignupEntity sellerSignupEntity = new SellerSignupEntity(sellerSignupPojo.getId(),
					sellerSignupPojo.getUsername(), sellerSignupPojo.getPassword(), sellerSignupPojo.getCompany(),
					sellerSignupPojo.getBrief(), sellerSignupPojo.getGst(), sellerSignupPojo.getAddress(),
					sellerSignupPojo.getEmail(), sellerSignupPojo.getWebsite(), sellerSignupPojo.getContact());

			CategoryEntity categoryEntity = new CategoryEntity(categoryPojo.getId(), categoryPojo.getName(),
					categoryPojo.getBrief());
			SubCategoryEntity subCategoryEntity = new SubCategoryEntity(subCategoryPojo.getId(),
					subCategoryPojo.getName(), categoryEntity, subCategoryPojo.getBrief(),
					subCategoryPojo.getGstPercent());

			ItemEntity itemEntity = new ItemEntity(itemPojo.getId(), itemPojo.getName(), sellerSignupEntity,
					subCategoryEntity, itemPojo.getPrice(), itemPojo.getDescription(), itemPojo.getStock(),
					itemPojo.getRemarks(), itemPojo.getImage());

			BillDetailsEntity billDetailsEntity = new BillDetailsEntity(billDetailsPojo.getId(), setbillEntity,
					itemEntity);
			//saving billdetailsentity to database via DAO
			
			billDetailsDao.save(billDetailsEntity);
		}
		LOG.info("Exited addBill()");
		BasicConfigurator.resetConfiguration();
	
		return billPojo;
	}

}
